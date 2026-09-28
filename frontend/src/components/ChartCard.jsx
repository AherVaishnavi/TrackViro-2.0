import { Bar, Doughnut } from "react-chartjs-2";
import {
  Chart as ChartJS,
  ArcElement,
  BarElement,
  CategoryScale,
  LinearScale,
  Tooltip,
  Legend,
} from "chart.js";

ChartJS.register(ArcElement, BarElement, CategoryScale, LinearScale, Tooltip, Legend);

const PALETTE = ["#057b98", "#09c2ef", "#9eeb47", "#f4a261", "#ec7e89", "#7c3aed", "#6b7a90"];

/**
 * Ported from the old .chart-card/.chart-title/.chart-wrap markup.
 * chartData matches the backend's ChartData record exactly:
 *   { labels: string[], data: number[] }
 */
export default function ChartCard({ title, chartData, type = "doughnut", large = false }) {
  const hasData = chartData && chartData.labels && chartData.labels.length > 0;

  const dataset = {
    labels: hasData ? chartData.labels : [],
    datasets: [
      {
        label: title,
        data: hasData ? chartData.data : [],
        backgroundColor: PALETTE,
        borderWidth: type === "bar" ? 0 : 2,
        borderColor: "#fff",
      },
    ],
  };

  return (
    <div className="chart-card">
      <div className="chart-title">{title}</div>
      <div className={large ? "chart-wrap-lg" : "chart-wrap"}>
        {hasData ? (
          type === "bar" ? (
            <Bar
              data={dataset}
              options={{ maintainAspectRatio: false, plugins: { legend: { display: false } } }}
            />
          ) : (
            <Doughnut
              data={dataset}
              options={{ maintainAspectRatio: false, plugins: { legend: { position: "bottom", labels: { boxWidth: 10, font: { size: 10 } } } } }}
            />
          )
        ) : (
          <p className="text-muted small mb-0">No data yet.</p>
        )}
      </div>
    </div>
  );
}
