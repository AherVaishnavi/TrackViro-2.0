package com.trackviro.backend.storage;

import com.trackviro.backend.exception.BusinessRuleException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.Set;
import java.util.UUID;

/**
 * NEW in Step 6 — nothing like this existed in Steps 1–5. The old
 * app's file handling lived inline in EmployeeController
 * (expense bills, using a hardcoded "C:/Corporate_Expense_Tracker/uploads/"
 * path that shadowed its own @Value field) and ProfileController
 * (profile pictures, correctly using its @Value field). Both are
 * consolidated here into one place, required because Step 6 asks
 * controllers to "handle multipart ... uploads appropriately" and no
 * upload mechanism has been built yet in this project.
 *
 * Two deliberate improvements over the old app, both flagged rather
 * than silent:
 *  1. Filenames are UUID-based, not "{timestamp}_{originalFilename}".
 *     The old naming scheme kept the original filename verbatim with
 *     no sanitization — a real (if minor) risk. This does not change
 *     any business rule, only how a stored file is named on disk.
 *  2. Content-type is validated before anything is written. The old
 *     app had no upload validation at all (documented in the
 *     migration checklist as issue S8). This is new behaviour, not a
 *     port of anything — a bad file type now fails with a clean 400
 *     instead of being accepted silently.
 *
 * REQUIRES a "file.upload-dir" property, which does not exist in
 * application.properties yet — see the Step 6 summary.
 */
@Component
public class FileStorageService {

    private static final Set<String> ALLOWED_BILL_TYPES =
            Set.of("image/jpeg", "image/png", "application/pdf");
    private static final Set<String> ALLOWED_IMAGE_TYPES =
            Set.of("image/jpeg", "image/png");

    @Value("${file.upload-dir}")
    private String uploadDir;

    /** Stores an expense bill. Returns the generated filename to save
     *  on Expense.billPath, or null if no file was provided (bills
     *  are optional, matching the old submit_expense.html form). */
    public String storeBill(MultipartFile file) {
        if (file == null || file.isEmpty()) return null;
        validate(file, ALLOWED_BILL_TYPES, "Bills must be JPEG, PNG, or PDF.");
        return save(file, "bills");
    }

    /** Stores a profile picture. Returns the generated filename, or
     *  null if none was provided (profile picture is optional). */
    public String storeProfilePicture(MultipartFile file) {
        if (file == null || file.isEmpty()) return null;
        validate(file, ALLOWED_IMAGE_TYPES, "Profile pictures must be JPEG or PNG.");
        return save(file, "profiles");
    }

    // ── Helpers ───────────────────────────────────────────────

    private void validate(MultipartFile file, Set<String> allowedTypes, String message) {
        String contentType = file.getContentType();
        if (contentType == null || !allowedTypes.contains(contentType)) {
            throw new BusinessRuleException("INVALID_FILE_TYPE", message, HttpStatus.BAD_REQUEST);
        }
        // Size is also capped by spring.servlet.multipart.max-file-size
        // (see the Step 6 summary); this is a second, explicit check
        // so the error is a clean BusinessRuleException rather than
        // whatever MaxUploadSizeExceededException produces by default.
        if (file.getSize() > 5L * 1024 * 1024) {
            throw new BusinessRuleException("FILE_TOO_LARGE",
                    "File exceeds the 5 MB limit.", HttpStatus.BAD_REQUEST);
        }
    }

    private String save(MultipartFile file, String subFolder) {
        try {
            Path dir = Paths.get(uploadDir, subFolder).toAbsolutePath().normalize();
            Files.createDirectories(dir);

            String original = file.getOriginalFilename();
            String extension = "";
            if (original != null && original.contains(".")) {
                extension = original.substring(original.lastIndexOf('.'));
            }
            String generatedName = UUID.randomUUID() + extension;

            Path target = dir.resolve(generatedName);
            Files.copy(file.getInputStream(), target, StandardCopyOption.REPLACE_EXISTING);

            return generatedName;
        } catch (IOException e) {
            throw new BusinessRuleException("UPLOAD_FAILED",
                    "Could not save the uploaded file. Please try again.",
                    HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }
}
