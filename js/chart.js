// AutoFuel Consumption Canvas Chart (chart.js)

export function renderConsumptionChart(canvas, refillsWithStats, averageKmL) {
  if (!canvas) return;

  const ctx = canvas.getContext('2d');
  const dpr = window.devicePixelRatio || 1;

  // Filter valid data in chronological order (oldest to newest)
  const validData = refillsWithStats
    .filter(r => r.kmPerLiter !== null && r.kmPerLiter > 0)
    .sort((a, b) => a.log.odometerKm - b.log.odometerKm);

  const rect = canvas.getBoundingClientRect();
  const width = rect.width || 400;
  const height = rect.height || 220;

  canvas.width = width * dpr;
  canvas.height = height * dpr;
  ctx.scale(dpr, dpr);

  ctx.clearRect(0, 0, width, height);

  if (validData.length === 0) {
    ctx.fillStyle = '#64748B';
    ctx.font = '13px "Outfit", sans-serif';
    ctx.textAlign = 'center';
    ctx.textBaseline = 'middle';
    ctx.fillText('Nenhum dado de km/L disponível ainda.', width / 2, height / 2 - 10);
    ctx.font = '11px "Outfit", sans-serif';
    ctx.fillText('Cadastre pelo menos 2 abastecimentos com tanque cheio.', width / 2, height / 2 + 12);
    return;
  }

  const padding = { top: 25, right: 30, bottom: 40, left: 45 };
  const chartW = width - padding.left - padding.right;
  const chartH = height - padding.top - padding.bottom;

  const values = validData.map(d => d.kmPerLiter);
  const minVal = Math.max(0, Math.floor(Math.min(...values, averageKmL || 10) - 2));
  const maxVal = Math.ceil(Math.max(...values, averageKmL || 15) + 2);
  const valRange = (maxVal - minVal) || 1;

  const getX = (idx) => {
    if (validData.length === 1) return padding.left + chartW / 2;
    return padding.left + (idx / (validData.length - 1)) * chartW;
  };

  const getY = (val) => {
    return padding.top + chartH - ((val - minVal) / valRange) * chartH;
  };

  const isDarkMode = document.documentElement.getAttribute('data-theme') !== 'light';
  const gridColor = isDarkMode ? 'rgba(255, 255, 255, 0.08)' : 'rgba(0, 0, 0, 0.08)';
  const textColor = isDarkMode ? '#94A3B8' : '#64748B';
  const primaryTeal = '#2DD4BF';
  const primaryAmber = '#F59E0B';

  // 1. Draw horizontal grid lines & Y labels
  ctx.strokeStyle = gridColor;
  ctx.lineWidth = 1;
  ctx.font = '11px "JetBrains Mono", monospace, sans-serif';
  ctx.fillStyle = textColor;
  ctx.textAlign = 'right';
  ctx.textBaseline = 'middle';

  const gridSteps = 4;
  for (let i = 0; i <= gridSteps; i++) {
    const val = minVal + (valRange / gridSteps) * i;
    const y = getY(val);
    ctx.beginPath();
    ctx.moveTo(padding.left, y);
    ctx.lineTo(width - padding.right, y);
    ctx.stroke();
    ctx.fillText(`${val.toFixed(1)}`, padding.left - 8, y);
  }

  // 2. Average dashed line
  if (averageKmL && averageKmL > 0) {
    const avgY = getY(averageKmL);
    ctx.save();
    ctx.strokeStyle = primaryAmber;
    ctx.lineWidth = 1.5;
    ctx.setLineDash([4, 4]);
    ctx.beginPath();
    ctx.moveTo(padding.left, avgY);
    ctx.lineTo(width - padding.right, avgY);
    ctx.stroke();

    ctx.fillStyle = primaryAmber;
    ctx.textAlign = 'left';
    ctx.font = '10px "Outfit", sans-serif';
    ctx.fillText(`Média: ${averageKmL.toFixed(1)} km/L`, padding.left + 6, avgY - 8);
    ctx.restore();
  }

  // 3. Draw area gradient under the curve
  const points = validData.map((d, idx) => ({ x: getX(idx), y: getY(d.kmPerLiter), item: d }));

  if (points.length > 1) {
    const gradient = ctx.createLinearGradient(0, padding.top, 0, padding.top + chartH);
    gradient.addColorStop(0, 'rgba(45, 212, 191, 0.35)');
    gradient.addColorStop(1, 'rgba(45, 212, 191, 0.00)');

    ctx.beginPath();
    ctx.moveTo(points[0].x, padding.top + chartH);
    ctx.lineTo(points[0].x, points[0].y);

    for (let i = 0; i < points.length - 1; i++) {
      const p0 = points[i];
      const p1 = points[i + 1];
      const cpX = (p0.x + p1.x) / 2;
      ctx.bezierCurveTo(cpX, p0.y, cpX, p1.y, p1.x, p1.y);
    }

    ctx.lineTo(points[points.length - 1].x, padding.top + chartH);
    ctx.closePath();
    ctx.fillStyle = gradient;
    ctx.fill();

    // 4. Draw smooth stroke curve
    ctx.beginPath();
    ctx.moveTo(points[0].x, points[0].y);
    for (let i = 0; i < points.length - 1; i++) {
      const p0 = points[i];
      const p1 = points[i + 1];
      const cpX = (p0.x + p1.x) / 2;
      ctx.bezierCurveTo(cpX, p0.y, cpX, p1.y, p1.x, p1.y);
    }
    ctx.strokeStyle = primaryTeal;
    ctx.lineWidth = 3;
    ctx.stroke();
  }

  // 5. Draw point dots and values
  points.forEach((p) => {
    // Outer glow circle
    ctx.beginPath();
    ctx.arc(p.x, p.y, 6, 0, Math.PI * 2);
    ctx.fillStyle = isDarkMode ? '#0F172A' : '#FFFFFF';
    ctx.fill();
    ctx.strokeStyle = primaryTeal;
    ctx.lineWidth = 2.5;
    ctx.stroke();

    // Inner dot
    ctx.beginPath();
    ctx.arc(p.x, p.y, 3, 0, Math.PI * 2);
    ctx.fillStyle = primaryTeal;
    ctx.fill();

    // Value badge above point
    ctx.font = 'bold 10px "JetBrains Mono", monospace';
    ctx.fillStyle = isDarkMode ? '#F1F5F9' : '#0F172A';
    ctx.textAlign = 'center';
    ctx.fillText(`${p.item.kmPerLiter.toFixed(1)}`, p.x, p.y - 12);

    // X Axis Labels (Dates)
    const dateObj = new Date(p.item.log.dateEpochMillis);
    const day = String(dateObj.getDate()).padStart(2, '0');
    const month = String(dateObj.getMonth() + 1).padStart(2, '0');
    const dateLabel = `${day}/${month}`;

    ctx.font = '10px "Outfit", sans-serif';
    ctx.fillStyle = textColor;
    ctx.textAlign = 'center';
    ctx.fillText(dateLabel, p.x, height - padding.bottom + 16);

    ctx.font = '9px "JetBrains Mono", monospace';
    ctx.fillStyle = textColor;
    ctx.fillText(`${p.item.log.odometerKm.toLocaleString('pt-BR')}k`, p.x, height - padding.bottom + 28);
  });
}
