class FinancialCalendar {
    constructor() {
        this.expenses = [];
        this.yearlyAdditions = [];
        this.balance = 0;
        this.dailyBudget = 0;
        this.currentDate = new Date();
        this.selectedMonth = this.currentDate.getMonth();
        this.selectedYear = this.currentDate.getFullYear();
        this.colorStops = {
                high: { threshold: 0.33, color: '#45c849' },
                medium: { threshold: 0.66, color: '#FFC107' },
                low: { threshold: 0.9, color: '#FF9800' },
                critical: { threshold: 1.0, color: '#F44336' }
            };

            this.borderColors = {
                current: '#2196F3',
                expense: '#F44336',
                addition: '#4CAF50'
            };

        this.init();
    }

    async init() {
        try {
            await this.fetchBalance();
            await this.fetchSalarySpentPercentage();
            await this.fetchYearlyAdditions(this.selectedYear);
            await this.fetchMonthData();
            this.renderCalendar();
            this.setupEventListeners();
        } catch (error) {
            console.error('Error initializing calendar:', error);
        }
    }

    async fetchBalance() {
        try {
            const response = await fetch('/api/user/get/balance');
            if (!response.ok) {
                throw new Error('Failed to fetch balance');
            }

            this.balance = await response.json();
            this.calculateDailyBudget();
        } catch (error) {
            console.error('Error fetching balance:', error);
        }
    }

    async fetchMonthData() {
        try {
            const response = await fetch('/api/expense/get/all/v2');
            if (!response.ok) {
                throw new Error('Failed to fetch expense data');
            }

            const data = await response.json();
            this.expenses = data;


            console.log('Loaded expense data:', this.expenses);


            const monthNames = ["JAN", "FEB", "MAR", "APR", "MAY", "JUN", "JUL", "AUG", "SEP", "OCT", "NOV", "DEC"];
            const currentMonthName = monthNames[this.selectedMonth];

            if (this.expenses.monthlyExpenses && this.expenses.monthlyExpenses[currentMonthName]) {
                console.log(`Found expenses for ${currentMonthName}:`,
                    this.expenses.monthlyExpenses[currentMonthName].length);
            } else {
                console.log(`No expenses found for ${currentMonthName}`);
            }
        } catch (error) {
            console.error('Error fetching month data:', error);
        }
    }

    calculateDailyBudget() {
        const daysInMonth = new Date(this.selectedYear, this.selectedMonth + 1, 0).getDate();

        if (daysInMonth > 0) {
            this.dailyBudget = this.balance / daysInMonth;
        } else {
            this.dailyBudget = 0;
        }
    }

    normalizeBudgetData(dayData) {
        let normalizedValue = 0;
        let waveHeight = 0;


        if (this.salarySpentPercentage !== undefined) {

            normalizedValue = Math.min(1, this.salarySpentPercentage);


            waveHeight = 20 + (normalizedValue * 80);
        } else {

            normalizedValue = 0.5;
            waveHeight = 60;
        }


        normalizedValue = Math.max(0, Math.min(1, normalizedValue));
        waveHeight = Math.max(20, Math.min(100, waveHeight));

        const color = this.getColorForValue(normalizedValue, dayData.isPast);

        return {
            normalizedValue,
            color,
            waveHeight
        };
    }

    getColorForValue(value, isPast) {
        if (isPast) {
            return '#AAAAAA';
        }


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
        calendar.innerHTML = '';

        const monthName = new Intl.DateTimeFormat('pt-BR', { month: 'long', year: 'numeric' }).format(
            new Date(this.selectedYear, this.selectedMonth)
        );

        const capitalizedMonth = monthName.charAt(0).toUpperCase() + monthName.slice(1);

        const monthHeader = document.createElement('h2');
        monthHeader.className = 'month-header';
        monthHeader.innerText = capitalizedMonth;
        calendar.appendChild(monthHeader);

        const weekdayNames = ['Dom', 'Seg', 'Ter', 'Qua', 'Qui', 'Sex', 'Sáb'];
        for (let i = 0; i < 7; i++) {
            const dayHeader = document.createElement('div');
            dayHeader.className = 'weekday-header';
            dayHeader.innerText = weekdayNames[i];
            calendar.appendChild(dayHeader);
        }

        const firstDay = new Date(this.selectedYear, this.selectedMonth, 1);
        const firstDayIndex = firstDay.getDay();

        for (let i = 0; i < firstDayIndex; i++) {
            const placeholder = document.createElement('div');
            placeholder.style.visibility = 'hidden';
            calendar.appendChild(placeholder);
        }

        const daysInMonth = new Date(this.selectedYear, this.selectedMonth + 1, 0).getDate();

        for (let day = 1; day <= daysInMonth; day++) {
            const dayDate = new Date(this.selectedYear, this.selectedMonth, day);
            const isToday = this.isCurrentDay(dayDate);
            const isPastDay = this.isPastDay(dayDate);

            const dayData = this.calculateDayData(day);
            const dayExpenses = this.getDayExpenses(day);
            const dayAdditions = this.getDayAdditions(day);

            const card = this.createDayCard(day, dayData, isToday, isPastDay, dayExpenses, dayAdditions);
            calendar.appendChild(card);
        }
    }

    calculateDayData(day) {
        return {
            day,
            remainingBudget: this.dailyBudget,
            isPast: this.isPastDay(new Date(this.selectedYear, this.selectedMonth, day))
        };
    }

    isCurrentDay(date) {
        const today = new Date();
        return date.getDate() === today.getDate() &&
            date.getMonth() === today.getMonth() &&
            date.getFullYear() === today.getFullYear();
    }

    isPastDay(date) {
        const today = new Date();
        today.setHours(0, 0, 0, 0);
        date.setHours(0, 0, 0, 0);
        return date < today;
    }

    getDayExpenses(day) {
        if (!this.expenses) {
            console.log("No expenses data available", this.expenses);
            return [];
        }


        const monthNames = ["JAN", "FEB", "MAR", "APR", "MAY", "JUN", "JUL", "AUG", "SEP", "OCT", "NOV", "DEC"];
        const currentMonthName = monthNames[this.selectedMonth];


        if (this.expenses.monthlyExpenses && this.expenses.monthlyExpenses[currentMonthName]) {
            const expensesForMonth = this.expenses.monthlyExpenses[currentMonthName];


            const matches = expensesForMonth.filter(expense => {

                if (Array.isArray(expense.expenseDate)) {
                    const expenseYear = expense.expenseDate[0];
                    const expenseMonth = expense.expenseDate[1];
                    const expenseDay = expense.expenseDate[2];

                    return expenseDay === day &&
                        expenseMonth === this.selectedMonth + 1 &&
                        expenseYear === this.selectedYear;
                }
                return false;
            });

            console.log(`Found ${matches.length} expenses for day ${day} in ${currentMonthName}`, matches);
            return matches;
        }

        return [];
    }

    async fetchYearlyAdditions(year) {
        try {
            const response = await fetch(`/api/additions/get/yearly?year=${year}`);
            if (!response.ok) {
                throw new Error('Failed to fetch yearly additions');
            }
            const data = await response.json();


            this.yearlyAdditions = data;
            console.log("Fetched yearly additions:", this.yearlyAdditions);
        } catch (error) {
            console.error('Error fetching yearly additions:', error);
            this.yearlyAdditions = [];
        }
    }


    getDayAdditions(day) {
        if (!this.yearlyAdditions || !Array.isArray(this.yearlyAdditions)) {
            console.log("No yearly additions data available", this.yearlyAdditions);
            return [];
        }

        const matches = this.yearlyAdditions.filter(addition => {

            if (Array.isArray(addition.createdAt)) {
                const additionYear = addition.createdAt[0];
                const additionMonth = addition.createdAt[1];
                const additionDay = addition.createdAt[2];

                return additionDay === day &&
                    additionMonth === this.selectedMonth + 1 &&
                    additionYear === this.selectedYear;
            }
            return false;
        });

        console.log(`Found ${matches.length} additions for day ${day}`, matches);
        return matches;
    }


    createDayCard(day, dayData, isToday, isPastDay, dayExpenses, dayAdditions) {
        const card = document.createElement('article');
        card.className = 'day-card';
        card.dataset.day = day;


        card.style.border = '1px solid transparent';


        const hasExpenses = dayExpenses && dayExpenses.length > 0;
        const hasAdditions = dayAdditions && dayAdditions.length > 0;


        if (hasExpenses) {
            card.classList.add('has-expense');
            card.dataset.expenseCount = dayExpenses.length;
            card.style.border = `2px solid ${this.borderColors.expense}`;
        }

        if (hasAdditions) {
            card.classList.add('has-addition');
            card.dataset.additionCount = dayAdditions.length;


            if (!hasExpenses) {
                card.style.border = `2px solid ${this.borderColors.addition}`;
            }
        }


        if (isToday && !hasExpenses && !hasAdditions) {
            card.classList.add('current-day');
            card.style.border = `2px solid ${this.borderColors.current}`;
        }

        if (isPastDay) {
            card.classList.add('past-day');
        }


        const budgetData = this.normalizeBudgetData(dayData);

        const waveContainer = document.createElement('div');
        waveContainer.className = 'wave-container';

        const svgNS = "http://www.w3.org/2000/svg";
        const waveSvg = document.createElementNS(svgNS, "svg");
        waveSvg.setAttribute("class", "wave-svg");
        waveSvg.setAttribute("viewBox", "0 0 200 100");
        waveSvg.setAttribute("preserveAspectRatio", "none");

        const wavePath = document.createElementNS(svgNS, "path");
        wavePath.setAttribute("fill", budgetData.color);
        wavePath.dataset.baseHeight = budgetData.waveHeight;

        wavePath.setAttribute("d", this.createWavePath(budgetData.waveHeight, 0, 0.02, 0));

        waveSvg.appendChild(wavePath);
        waveContainer.appendChild(waveSvg);

        const monthName = new Intl.DateTimeFormat('pt-BR', { month: 'long' }).format(
            new Date(this.selectedYear, this.selectedMonth)
        );
        const capitalizedMonth = monthName.charAt(0).toUpperCase() + monthName.slice(1);

        const monthEl = document.createElement('div');
        monthEl.className = 'month';
        monthEl.innerText = capitalizedMonth;
        monthEl.style.color = budgetData.color;

        card.appendChild(monthEl);

        const dayNumber = document.createElement('div');
        dayNumber.className = 'day-number contrasting-text';
        dayNumber.innerText = day;

        dayNumber.style.webkitTextStroke = `1px ${budgetData.color}`;
        dayNumber.style.textStroke = `1px ${budgetData.color}`;

        card.appendChild(dayNumber);

        const remaining = document.createElement('div');
        remaining.className = 'remaining contrasting-text';
        remaining.innerText = `R$ ${dayData.remainingBudget.toFixed(2).replace('.', ',')}`;
        card.appendChild(remaining);

        card.appendChild(waveContainer);

        if (hasExpenses) {
            this.createExpenseHoverCard(card, dayExpenses);
        }

        if (hasAdditions) {
            this.createAdditionHoverInfo(card, dayAdditions);
        }

        this.setupWaveAnimation(card, wavePath, budgetData.waveHeight);


        if (hasExpenses || hasAdditions) {
            card.style.cursor = 'pointer';
        }

        return card;
    }

    createExpenseHoverCard(card, expenses) {
        const hoverCard = document.createElement('div');
        hoverCard.className = 'expense-hover-card';

        let hoverContent = '<h3>Despesas</h3>';

        const sortedExpenses = [...expenses].sort((a, b) => b.expenseAmount - a.expenseAmount);

        sortedExpenses.forEach(expense => {
            const formattedAmount = expense.expenseAmount.toFixed(2).replace('.', ',');
            const categoryLabel = this.getCategoryLabel(expense.expenseCategory);

            hoverContent += `
            <div class="expense-item">
                <div class="expense-name">${expense.expenseName || 'Sem nome'}</div>
                <div class="expense-amount">R$ ${formattedAmount}</div>
                <div class="expense-category">${categoryLabel}</div>
                ${expense.description ? `<div class="expense-description">${expense.description}</div>` : ''}
                <div class="expense-recurrent">${expense.isRecurrent ? 'Recorrente' : 'Não recorrente'}</div>
            </div>
        `;
        });

        hoverCard.innerHTML = hoverContent;

        hoverCard.style.position = 'absolute';
        hoverCard.style.left = '100%';
        hoverCard.style.top = '0';
        hoverCard.style.zIndex = '100';
        hoverCard.style.background = '#fff';
        hoverCard.style.border = '1px solid #ddd';
        hoverCard.style.borderRadius = '4px';
        hoverCard.style.padding = '10px';
        hoverCard.style.boxShadow = '0 2px 8px rgba(0,0,0,0.15)';
        hoverCard.style.minWidth = '250px';

        card.style.position = 'relative';
        card.appendChild(hoverCard);

        hoverCard.style.display = 'none';


        card.addEventListener('mouseenter', () => {
            hoverCard.style.display = 'block';
        });

        card.addEventListener('mouseleave', () => {
            hoverCard.style.display = 'none';
        });


        card.addEventListener('click', (e) => {

            if (hoverCard.style.display === 'none') {
                hoverCard.style.display = 'block';
            } else {
                hoverCard.style.display = 'none';
            }
            e.stopPropagation();
        });


        document.addEventListener('click', (e) => {
            if (!card.contains(e.target)) {
                hoverCard.style.display = 'none';
            }
        });
    }

    createAdditionHoverInfo(card, additions) {
        const hoverInfo = document.createElement('div');
        hoverInfo.className = 'addition-hover-info';

        let hoverContent = '<h3>Adições ao Saldo</h3>';

        const sortedAdditions = [...additions].sort((a, b) => b.amount - a.amount);

        sortedAdditions.forEach(addition => {
            const formattedAmount = addition.amount.toFixed(2).replace('.', ',');

            hoverContent += `
            <div class="addition-item">
                <div class="addition-amount">+ R$ ${formattedAmount}</div>
                ${addition.description ? `<div class="addition-description">${addition.description}</div>` : ''}
            </div>
        `;
        });

        hoverInfo.innerHTML = hoverContent;

        hoverInfo.style.position = 'absolute';
        hoverInfo.style.left = '100%';
        hoverInfo.style.top = '0';
        hoverInfo.style.zIndex = '100';
        hoverInfo.style.background = '#fff';
        hoverInfo.style.border = '1px solid #ddd';
        hoverInfo.style.borderRadius = '4px';
        hoverInfo.style.padding = '10px';
        hoverInfo.style.boxShadow = '0 2px 8px rgba(0,0,0,0.15)';
        hoverInfo.style.minWidth = '250px';

        card.style.position = 'relative';
        card.appendChild(hoverInfo);

        hoverInfo.style.display = 'none';


        card.addEventListener('mouseenter', () => {
            hoverInfo.style.display = 'block';
        });

        card.addEventListener('mouseleave', () => {
            hoverInfo.style.display = 'none';
        });


        card.addEventListener('click', (e) => {

            if (hoverInfo.style.display === 'none') {
                hoverInfo.style.display = 'block';
            } else {
                hoverInfo.style.display = 'none';
            }
            e.stopPropagation();
        });
    }

    getCategoryLabel(categoryCode) {
        const categories = {
            'FOOD': 'Alimentação',
            'TRANSPORT': 'Transporte',
            'HEALTH': 'Saúde',
            'EDUCATION': 'Educação',
            'ENTERTAINMENT': 'Lazer',
            'UTILITIES': 'Serviços Públicos',
            'HOUSING': 'Moradia',
            'PERSONAL_CARE': 'Cuidados Pessoais',
            'INSURANCE': 'Seguro',
            'SAVINGS': 'Poupança',
            'OTHER': 'Outros'
        };

        return categories[categoryCode] || categoryCode;
    }

    async fetchSalarySpentPercentage() {
        try {
            const response = await fetch('/api/user/get/salary/spent');
            if (!response.ok) {
                throw new Error('Failed to fetch salary spent percentage');
            }

            const percentage = await response.json();
            this.salarySpentPercentage = percentage;
            console.log('Salary spent percentage:', this.salarySpentPercentage);
            return percentage;
        } catch (error) {
            console.error('Error fetching salary spent percentage:', error);
            this.salarySpentPercentage = 0;
            return 0;
        }
    }

    setupWaveAnimation(card, wavePath, baseHeight) {
        let animationId = null;
        let phase = 0;
        let amplitude = 3;
        let frequency = 0.02;

        const animateWave = () => {
            phase += 0.1;
            const path = this.createWavePath(baseHeight, amplitude, frequency, phase);
            wavePath.setAttribute('d', path);
            animationId = requestAnimationFrame(animateWave);
        };

        const handleMouseMove = (e) => {
            const rect = card.getBoundingClientRect();
            const relX = (e.clientX - rect.left) / rect.width;
            const relY = (e.clientY - rect.top) / rect.height;

            frequency = 0.01 + (relX * 0.05);
            amplitude = 2 + ((1 - relY) * 10);

            const currentHeight = parseFloat(wavePath.dataset.baseHeight);
            const path = this.createWavePath(currentHeight, amplitude, frequency, phase);
            wavePath.setAttribute('d', path);
        };

        card.addEventListener('mouseenter', () => {
            const currentHeight = parseFloat(wavePath.dataset.baseHeight);
            if (!animationId) {
                animationId = requestAnimationFrame(animateWave);
            }
            card.addEventListener('mousemove', handleMouseMove);
        });

        card.addEventListener('mouseleave', () => {
            card.removeEventListener('mousemove', handleMouseMove);

            const currentHeight = parseFloat(wavePath.dataset.baseHeight);
            const transitionToRest = () => {
                amplitude *= 0.85;
                const path = this.createWavePath(currentHeight, amplitude, frequency, phase);
                wavePath.setAttribute('d', path);

                if (amplitude > 0.1) {
                    requestAnimationFrame(transitionToRest);
                } else {
                    const finalPath = this.createWavePath(currentHeight, 0, frequency, 0);
                    wavePath.setAttribute('d', finalPath);

                    if (animationId) {
                        cancelAnimationFrame(animationId);
                        animationId = null;
                    }
                    phase = 0;
                }
            };

            transitionToRest();
        });
    }

    animateBudgetChange(cardElement) {
        const wavePath = cardElement.querySelector('.wave-svg path');
        if (!wavePath) return;

        const dayNumber = parseInt(cardElement.dataset.day, 10);
        const isPast = this.isPastDay(new Date(this.selectedYear, this.selectedMonth, dayNumber));
        const waveColor = isPast ? '#AAAAAA' : '#45c849';
        const baseHeight = 50;

        wavePath.setAttribute('fill', waveColor);
        wavePath.dataset.baseHeight = baseHeight;

        const monthEl = cardElement.querySelector('.month');
        if (monthEl) {
            monthEl.style.color = waveColor;
        }

        const dayNumberEl = cardElement.querySelector('.day-number');
        if (dayNumberEl) {
            dayNumberEl.style.webkitTextStroke = `1px ${waveColor}`;
            dayNumberEl.style.textStroke = `1px ${waveColor}`;
        }

        const remainingEl = cardElement.querySelector('.remaining');
        if (remainingEl) {
            remainingEl.innerText = `R$ ${this.dailyBudget.toFixed(2).replace('.', ',')}`;
        }

        const path = this.createWavePath(baseHeight, 0, 0.02, 0);
        wavePath.setAttribute('d', path);
    }

    setupEventListeners() {
        const currentDateTime = new Date().toISOString().slice(0, 16);
        document.getElementById('expenseDate').value = currentDateTime;

        document.getElementById('expense-form').addEventListener('submit', async (e) => {
            e.preventDefault();

            const expenseDate = document.getElementById('expenseDate').value;
            const isRecurrent = document.getElementById('isRecurrent').checked;
            const expenseAmount = parseFloat(document.getElementById('expenseAmount').value);
            const expenseName = document.getElementById('expenseName').value;
            const expenseCategory = document.getElementById('expenseCategory').value;
            const description = document.getElementById('description').value;

            const expenseDTO = {
                expenseDate: expenseDate,
                isRecurrent: isRecurrent,
                expenseAmount: expenseAmount,
                expenseName: expenseName,
                expenseCategory: expenseCategory,
                description: description
            };

            try {
                const response = await fetch('/api/expense/register', {
                    method: 'POST',
                    headers: {
                        'Content-Type': 'application/json'
                    },
                    body: JSON.stringify(expenseDTO)
                });

                if (!response.ok) {
                    throw new Error('Failed to register expense');
                }

                const oldBalance = this.balance;
                const oldDailyBudget = this.dailyBudget;

                const newBalance = await response.json();
                this.balance = newBalance;


                await this.fetchSalarySpentPercentage();

                this.calculateDailyBudget();
                await this.fetchMonthData();

                const expenseDateObj = new Date(expenseDate);
                const expenseDay = expenseDateObj.getDate();

                const affectedCard = document.querySelector(`.day-card[data-day="${expenseDay}"]`);
                if (affectedCard) {
                    const oldDayData = {
                        day: expenseDay,
                        remainingBudget: oldDailyBudget,
                        isPast: false
                    };

                    const newDayData = this.calculateDayData(expenseDay);
                    this.animateBudgetChange(affectedCard, oldDayData, newDayData);
                }

                const daysInMonth = new Date(this.selectedYear, this.selectedMonth + 1, 0).getDate();
                for (let day = 1; day <= daysInMonth; day++) {
                    if (day !== expenseDay) {
                        const dayCard = document.querySelector(`.day-card[data-day="${day}"]`);
                        if (dayCard) {
                            const oldDayData = {
                                day: day,
                                remainingBudget: oldDailyBudget,
                                isPast: false
                            };
                            const newDayData = this.calculateDayData(day);
                            this.animateBudgetChange(dayCard, oldDayData, newDayData);
                        }
                    }
                }

                e.target.reset();
                document.getElementById('expenseDate').value = currentDateTime;

                alert('Despesa registrada com sucesso!');
            } catch (error) {
                console.error('Error registering expense:', error);
                alert('Erro ao registrar despesa. Por favor, tente novamente.');
            }
        });

        const addBalanceBtn = document.querySelector('.add-balance .submit-btn');
        addBalanceBtn.addEventListener('click', async () => {
            const balanceAmount = parseFloat(document.getElementById('balance-amount').value);
            const balanceDescription = document.getElementById('balance-description').value;

            if (!balanceAmount || isNaN(balanceAmount)) {
                alert('Por favor, insira um valor válido.');
                return;
            }

            const additionDTO = {
                amount: balanceAmount,
                description: balanceDescription
            };

            try {
                const response = await fetch('/api/additions/add/balance', {
                    method: 'POST',
                    headers: {
                        'Content-Type': 'application/json'
                    },
                    body: JSON.stringify(additionDTO)
                });

                if (!response.ok) {
                    throw new Error('Failed to add to balance');
                }

                const oldBalance = this.balance;
                const oldDailyBudget = this.dailyBudget;

                const newBalance = await response.json();
                this.balance = newBalance;


                await this.fetchSalarySpentPercentage();

                this.calculateDailyBudget();
                await this.fetchMonthData();

                const daysInMonth = new Date(this.selectedYear, this.selectedMonth + 1, 0).getDate();
                for (let day = 1; day <= daysInMonth; day++) {
                    const dayCard = document.querySelector(`.day-card[data-day="${day}"]`);
                    if (dayCard) {
                        const oldDayData = {
                            day: day,
                            remainingBudget: oldDailyBudget,
                            isPast: false
                        };
                        const newDayData = this.calculateDayData(day);
                        this.animateBudgetChange(dayCard, oldDayData, newDayData);
                    }
                }

                document.getElementById('balance-amount').value = '';
                document.getElementById('balance-description').value = '';

                alert('Valor adicionado ao saldo com sucesso!');
            } catch (error) {
                console.error('Error adding to balance:', error);
                alert('Erro ao adicionar ao saldo. Por favor, tente novamente.');
            }
        });
    }
}

document.addEventListener('DOMContentLoaded', () => {
    new FinancialCalendar();
});