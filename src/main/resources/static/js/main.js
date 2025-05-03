 function generateMonthData() {
    const daysInMonth = 30;
    const data = [];
    let initialBudget = 50.00;

    for (let day = 1; day <= daysInMonth; day++) {
    let diminishingFactor;

    if (day <= 10) {
    diminishingFactor = 0.93 + (Math.random() * 0.04);
} else if (day <= 20) {
    diminishingFactor = 0.88 + (Math.random() * 0.04);
} else {
    diminishingFactor = 0.80 + (Math.random() * 0.05);
}

    const randomVariation = (Math.random() * 0.06) - 0.03;
    const effectiveFactor = diminishingFactor + randomVariation;

    if (day === 1) {
    data.push({ day, reais: initialBudget });
} else {
    const previousValue = data[day - 2].reais;
    let newValue = previousValue * effectiveFactor;

    if (day < 25) {
    newValue = Math.max(newValue, (daysInMonth - day) * 0.3);
} else {
    newValue = Math.max(newValue, 0.10);
}

    data.push({ day, reais: parseFloat(newValue.toFixed(2)) });
}
}

    return data;
}

    const budgetData = generateMonthData();

    class WaveAnimation {
    constructor() {
    this.colorStops = {
    high: { threshold: 0.33, color: '#2ecc71' },
    medium: { threshold: 0.66, color: '#f1c40f' },
    low: { threshold: 0.9, color: '#e67e22' },
    critical: { threshold: 1.0, color: '#e74c3c' }
};

    this.normalizeBudgetData();
    this.renderCalendar();
}

    normalizeBudgetData() {
    const values = budgetData.map(item => item.reais);
    const maxReais = Math.max(...values);
    const minReais = Math.min(...values);
    const range = maxReais - minReais;

    budgetData.forEach(item => {
    item.normalizedValue = 1 - ((item.reais - minReais) / range);
    item.color = this.getColorForValue(item.normalizedValue);
    item.waveHeight = 20 + (item.normalizedValue * 65);
});
}

    getColorForValue(value) {
    if (value <= this.colorStops.high.threshold) {
    return this.colorStops.high.color;
} else if (value <= this.colorStops.medium.threshold) {
    return this.colorStops.medium.color;
} else if (value <= this.colorStops.low.threshold) {
    return this.colorStops.low.color;
} else {
    return this.colorStops.critical.color;
}
}

    createWavePath(baseHeight, amplitude, frequency, phase) {
    const width = 200;
    const points = [];

    for (let x = 0; x <= width; x += 1) {
    const y = baseHeight - (amplitude * Math.sin((x * frequency) + phase));
    points.push(`${x},${y}`);
}

    points.push(`${width},100 0,100`);

    return `M0,${baseHeight} L${points.join(' L')} Z`;
}

    renderCalendar() {
    const calendar = document.getElementById('calendar');
    calendar.innerHTML = '<h2 class="month-header">Maio 2025</h2>';

    
    const weekdayNames = ['Dom', 'Seg', 'Ter', 'Qua', 'Qui', 'Sex', 'Sáb'];
    for (let i = 0; i < 7; i++) {
    const dayHeader = document.createElement('div');
    dayHeader.className = 'weekday-header';
    dayHeader.innerText = weekdayNames[i];
    calendar.appendChild(dayHeader);
}

    
    const firstDayIndex = 4;


    for (let i = 0; i < firstDayIndex; i++) {
    const placeholder = document.createElement('div');
    placeholder.style.visibility = 'hidden';
    calendar.appendChild(placeholder);
}

    budgetData.forEach(item => {
    const card = document.createElement('article');
    card.className = 'day-card';
    card.dataset.day = item.day;
    card.dataset.normalized = item.normalizedValue;

    const waveContainer = document.createElement('div');
    waveContainer.className = 'wave-container';

    const svgNS = "http://www.w3.org/2000/svg";
    const waveSvg = document.createElementNS(svgNS, "svg");
    waveSvg.setAttribute("class", "wave-svg");
    waveSvg.setAttribute("viewBox", "0 0 200 100");
    waveSvg.setAttribute("preserveAspectRatio", "none");

    const wavePath = document.createElementNS(svgNS, "path");
    wavePath.setAttribute("fill", item.color);

    const baseHeight = item.waveHeight;
    wavePath.setAttribute("d", this.createWavePath(baseHeight, 0, 0.02, 0));

    waveSvg.appendChild(wavePath);
    waveContainer.appendChild(waveSvg);

    const currentDate = new Date();
    const monthName = new Intl.DateTimeFormat('pt-BR', { month: 'long' }).format(currentDate);
    const capitalizedMonth = monthName.charAt(0).toUpperCase() + monthName.slice(1);

    const monthEl = document.createElement('div');
    monthEl.className = 'month';
    monthEl.innerText = capitalizedMonth;
    monthEl.style.color = item.color;

    card.appendChild(monthEl);

    const dayNumber = document.createElement('div');
    dayNumber.className = 'day-number contrasting-text';
    dayNumber.innerText = item.day;
    card.appendChild(dayNumber);

    const remaining = document.createElement('div');
    remaining.className = 'remaining contrasting-text';
    remaining.innerText = `R$ ${item.reais.toFixed(2).replace('.', ',')}`;
    card.appendChild(remaining);

    card.appendChild(waveContainer);
    calendar.appendChild(card);

    const waveData = {
    card,
    waveSvg,
    wavePath,
    baseHeight,
    dayNumber,
    remaining,
    color: item.color,
    animation: null,
    phase: 0,
    amplitude: 3,
    frequency: 0.02
};

    this.setupHoverEffects(waveData);
});
}

    setupHoverEffects(waveData) {
    const { card, wavePath, waveSvg } = waveData;
    let animationId = null;

    const animateWave = () => {
    waveData.phase += 0.1;

    const path = this.createWavePath(
    waveData.baseHeight,
    waveData.amplitude,
    waveData.frequency,
    waveData.phase
    );

    wavePath.setAttribute('d', path);
    animationId = requestAnimationFrame(animateWave);
};

    const handleMouseMove = (e) => {
    const rect = card.getBoundingClientRect();
    const relX = (e.clientX - rect.left) / rect.width;
    const relY = (e.clientY - rect.top) / rect.height;

    waveData.frequency = 0.01 + (relX * 0.05);
    waveData.amplitude = 2 + ((1 - relY) * 10);
};

    card.addEventListener('mouseenter', () => {
    if (!animationId) {
    animationId = requestAnimationFrame(animateWave);
}
    card.addEventListener('mousemove', handleMouseMove);
});

    card.addEventListener('mouseleave', () => {
    card.removeEventListener('mousemove', handleMouseMove);

    const transitionToRest = () => {
    waveData.amplitude *= 0.85;

    const path = this.createWavePath(
    waveData.baseHeight,
    waveData.amplitude,
    waveData.frequency,
    waveData.phase
    );

    wavePath.setAttribute('d', path);

    if (waveData.amplitude > 0.1) {
    requestAnimationFrame(transitionToRest);
} else {
    const finalPath = this.createWavePath(waveData.baseHeight, 0, waveData.frequency, 0);
    wavePath.setAttribute('d', finalPath);

    if (animationId) {
    cancelAnimationFrame(animationId);
    animationId = null;
}

    waveData.phase = 0;
}
};

    transitionToRest();
});
}
}

    document.addEventListener('DOMContentLoaded', () => {
    new WaveAnimation();

    document.getElementById('expense-form').addEventListener('submit', function(e) {
    e.preventDefault();

    const formData = {
    expenseDate: document.getElementById('expenseDate').value,
    isRecurrent: document.getElementById('isRecurrent').checked,
    expenseAmount: document.getElementById('expenseAmount').value,
    expenseName: document.getElementById('expenseName').value,
    expenseCategory: document.getElementById('expenseCategory').value,
    description: document.getElementById('description').value
};

    console.log('Expense submitted:', formData);

    this.reset();
    alert('Despesa registrada com sucesso!');
});

    const currentDateTime = new Date().toISOString().slice(0, 16);
    document.getElementById('expenseDate').value = currentDateTime;
});

