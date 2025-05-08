document.addEventListener('DOMContentLoaded', function() {

    const monthsGrid = document.getElementById('months-grid');
    const currentYearElement = document.getElementById('current-year');
    const prevYearButton = document.getElementById('prev-year');
    const nextYearButton = document.getElementById('next-year');
    const currentSalaryElement = document.getElementById('current-salary');
    const currentBalanceElement = document.getElementById('current-balance');

    const updateSalaryBtn = document.getElementById('update-salary-btn');
    const updateSalaryDateBtn = document.getElementById('update-salary-date-btn');
    const salaryModal = document.getElementById('update-salary-modal');
    const salaryDateModal = document.getElementById('update-salary-date-modal');
    const closeSalaryModal = document.getElementById('close-salary-modal');
    const closeSalaryDateModal = document.getElementById('close-salary-date-modal');
    const cancelSalaryBtn = document.getElementById('cancel-salary-btn');
    const cancelSalaryDateBtn = document.getElementById('cancel-salary-date-btn');
    const saveSalaryBtn = document.getElementById('save-salary-btn');
    const saveSalaryDateBtn = document.getElementById('save-salary-date-btn');

    const today = new Date();
    let currentYear = today.getFullYear();
    const currentMonth = today.getMonth();
    const currentDate = today.getDate();

    let expenseData = {};
    let incomeData = {};



    const monthNames = [
        'Janeiro', 'Fevereiro', 'Março', 'Abril',
        'Maio', 'Junho', 'Julho', 'Agosto',
        'Setembro', 'Outubro', 'Novembro', 'Dezembro'
    ];


    const weekdayNames = ['D', 'S', 'T', 'Q', 'Q', 'S', 'S'];


    const getDaysInMonth = (year, month) => {
        return new Date(year, month + 1, 0).getDate();
    };


    const getFirstDayOfMonth = (year, month) => {
        return new Date(year, month, 1).getDay();
    };


    const formatCurrency = (value) => {
        return new Intl.NumberFormat('pt-BR', {
            style: 'currency',
            currency: 'BRL'
        }).format(value);
    };


    const fetchExpensesByMonth = async () => {
        try {
            const response = await fetch('/api/expense/get/all/v2');
            if (!response.ok) {
                throw new Error(`HTTP error! status: ${response.status}`);
            }
            return await response.json();
        } catch (error) {
            console.error("Could not fetch expenses:", error);
            return null;
        }
    };


    const fetchBalance = async () => {
        try {
            const response = await fetch('/api/user/get/balance');
            if (!response.ok) {
                throw new Error(`HTTP error! status: ${response.status}`);
            }
            const balance = await response.json();
            currentBalanceElement.textContent = formatCurrency(balance);
            return balance;
        } catch (error) {
            console.error("Could not fetch balance:", error);
            currentBalanceElement.textContent = "Erro ao carregar";
            return null;
        }
    };


    const fetchSalary = async () => {
        try {
            const response = await fetch('/api/user/get/salary');
            if (!response.ok) {
                throw new Error(`HTTP error! status: ${response.status}`);
            }
            const salary = await response.json();
            currentSalaryElement.textContent = formatCurrency(salary);
            return salary;
        } catch (error) {
            console.error("Could not fetch salary:", error);
            currentSalaryElement.textContent = "Erro ao carregar";
            return null;
        }
    };


    const updateSalary = async (amount) => {
        try {
            const response = await fetch(`/api/additions/salary/update?salaryAmount=${amount}`, {
                method: 'PUT'
            });
            if (!response.ok) {
                throw new Error(`HTTP error! status: ${response.status}`);
            }

            fetchSalary();
            return true;
        } catch (error) {
            console.error("Could not update salary:", error);
            return false;
        }
    };


    const updateSalaryDate = async (date) => {
        try {
            const response = await fetch(`/api/additions/salary/date/update?salaryDate=${date}`, {
                method: 'PUT'
            });
            if (!response.ok) {
                throw new Error(`HTTP error! status: ${response.status}`);
            }
            return true;
        } catch (error) {
            console.error("Could not update salary date:", error);
            return false;
        }
    };


    const fetchYearlyAdditions = async (year) => {
        try {
            const response = await fetch(`/api/additions/get/yearly?year=${year}`);
            if (!response.ok) {
                throw new Error(`HTTP error! status: ${response.status}`);
            }
            return await response.json();
        } catch (error) {
            console.error("Could not fetch yearly additions:", error);
            return null;
        }
    };


    let expenseDays = {};

    let incomeDays = {};


    const processExpenseData = (data) => {
        expenseData = {};
        expenseDays = {};


        if (data && data.monthlyExpenses) {

            Object.keys(data.monthlyExpenses).forEach(monthKey => {
                const expenses = data.monthlyExpenses[monthKey];

                if (Array.isArray(expenses)) {
                    expenses.forEach(expense => {
                        if (expense.expenseDate && expense.expenseDate.length >= 3) {
                            const year = expense.expenseDate[0];
                            const month = expense.expenseDate[1] - 1;
                            const day = expense.expenseDate[2];


                            if (year === currentYear) {

                                if (!expenseDays[month]) {
                                    expenseDays[month] = {};
                                }


                                if (!expenseData[month]) {
                                    expenseData[month] = {};
                                }
                                if (!expenseData[month][day]) {
                                    expenseData[month][day] = [];
                                }


                                expenseDays[month][day] = true;


                                expenseData[month][day].push({
                                    description: expense.expenseName || 'Despesa',
                                    value: expense.expenseAmount || 0
                                });
                            }
                        }
                    });
                }
            });
        }
    };


    const processIncomeData = (data) => {
        incomeData = {};
        incomeDays = {};


        if (data && Array.isArray(data)) {
            data.forEach(addition => {
                if (addition.createdAt && Array.isArray(addition.createdAt) && addition.createdAt.length >= 3) {
                    const year = addition.createdAt[0];
                    const month = addition.createdAt[1] - 1;
                    const day = addition.createdAt[2];


                    if (year === currentYear) {

                        if (!incomeDays[month]) {
                            incomeDays[month] = {};
                        }


                        if (!incomeData[month]) {
                            incomeData[month] = {};
                        }
                        if (!incomeData[month][day]) {
                            incomeData[month][day] = [];
                        }


                        incomeDays[month][day] = true;


                        incomeData[month][day].push({
                            description: addition.description || 'Receita',
                            value: addition.amount || 0
                        });
                    }
                }
            });
        }
    };

    const createDayTooltip = (month, day) => {
        const tooltip = document.createElement('div');
        tooltip.className = 'day-tooltip';
        const MAX_ITEMS = 4;

        const title = document.createElement('div');
        title.className = 'tooltip-title';
        title.textContent = `${day} de ${monthNames[month]}`;
        tooltip.appendChild(title);

        if (incomeData[month] && incomeData[month][day] && incomeData[month][day].length > 0) {
            const incomeSection = document.createElement('div');
            incomeSection.className = 'tooltip-section';

            const incomeHeading = document.createElement('div');
            incomeHeading.className = 'tooltip-heading';
            incomeHeading.textContent = 'Recebimentos:';
            incomeSection.appendChild(incomeHeading);

            let incomeTotal = 0;
            const incomeItems = incomeData[month][day];

            incomeItems.forEach(income => {
                incomeTotal += parseFloat(income.value || 0);
            });


            if (incomeItems.length > MAX_ITEMS) {
                const summaryItem = document.createElement('div');
                summaryItem.className = 'tooltip-item';

                const summaryLabel = document.createElement('span');
                summaryLabel.textContent = `${incomeItems.length} recebimentos`;
                summaryLabel.style.fontWeight = 'bold';

                const summaryValue = document.createElement('span');
                summaryValue.className = 'tooltip-value income-value';
                summaryValue.textContent = formatCurrency(incomeTotal);

                summaryItem.appendChild(summaryLabel);
                summaryItem.appendChild(summaryValue);
                incomeSection.appendChild(summaryItem);
            } else {

                incomeItems.forEach((income, idx) => {
                    const incomeItem = document.createElement('div');
                    incomeItem.className = 'tooltip-item';

                    const descSpan = document.createElement('span');
                    descSpan.textContent = income.description || 'Receita';

                    const valueSpan = document.createElement('span');
                    valueSpan.className = 'tooltip-value income-value';
                    valueSpan.textContent = formatCurrency(income.value);

                    incomeItem.appendChild(descSpan);
                    incomeItem.appendChild(valueSpan);
                    incomeSection.appendChild(incomeItem);


                    if (idx < incomeItems.length - 1) {
                        const divider = document.createElement('div');
                        divider.className = 'tooltip-divider';
                        incomeSection.appendChild(divider);
                    }
                });


                if (incomeItems.length > 1) {
                    const divider = document.createElement('div');
                    divider.className = 'tooltip-divider';
                    incomeSection.appendChild(divider);

                    const totalItem = document.createElement('div');
                    totalItem.className = 'tooltip-item';

                    const totalLabel = document.createElement('span');
                    totalLabel.textContent = 'Total receitas:';
                    totalLabel.style.fontWeight = 'bold';

                    const totalValue = document.createElement('span');
                    totalValue.className = 'tooltip-value income-value';
                    totalValue.textContent = formatCurrency(incomeTotal);

                    totalItem.appendChild(totalLabel);
                    totalItem.appendChild(totalValue);
                    incomeSection.appendChild(totalItem);
                }
            }

            tooltip.appendChild(incomeSection);
        }


        if (incomeData[month] && incomeData[month][day] && incomeData[month][day].length > 0 &&
            expenseData[month] && expenseData[month][day] && expenseData[month][day].length > 0) {
            const sectionDivider = document.createElement('div');
            sectionDivider.className = 'tooltip-divider';
            tooltip.appendChild(sectionDivider);
        }


        if (expenseData[month] && expenseData[month][day] && expenseData[month][day].length > 0) {
            const expenseSection = document.createElement('div');
            expenseSection.className = 'tooltip-section';

            const expenseHeading = document.createElement('div');
            expenseHeading.className = 'tooltip-heading';
            expenseHeading.textContent = 'Gastos:';
            expenseSection.appendChild(expenseHeading);

            let expenseTotal = 0;
            const expenseItems = expenseData[month][day];


            expenseItems.forEach(expense => {
                expenseTotal += parseFloat(expense.value || 0);
            });


            if (expenseItems.length > MAX_ITEMS) {
                const summaryItem = document.createElement('div');
                summaryItem.className = 'tooltip-item';

                const summaryLabel = document.createElement('span');
                summaryLabel.textContent = `${expenseItems.length} gastos`;
                summaryLabel.style.fontWeight = 'bold';

                const summaryValue = document.createElement('span');
                summaryValue.className = 'tooltip-value expense-value';
                summaryValue.textContent = formatCurrency(expenseTotal);

                summaryItem.appendChild(summaryLabel);
                summaryItem.appendChild(summaryValue);
                expenseSection.appendChild(summaryItem);
            } else {

                expenseItems.forEach((expense, idx) => {
                    const expenseItem = document.createElement('div');
                    expenseItem.className = 'tooltip-item';

                    const descSpan = document.createElement('span');
                    descSpan.textContent = expense.description || 'Despesa';

                    const valueSpan = document.createElement('span');
                    valueSpan.className = 'tooltip-value expense-value';
                    valueSpan.textContent = formatCurrency(expense.value);

                    expenseItem.appendChild(descSpan);
                    expenseItem.appendChild(valueSpan);
                    expenseSection.appendChild(expenseItem);


                    if (idx < expenseItems.length - 1) {
                        const divider = document.createElement('div');
                        divider.className = 'tooltip-divider';
                        expenseSection.appendChild(divider);
                    }
                });


                if (expenseItems.length > 1) {
                    const divider = document.createElement('div');
                    divider.className = 'tooltip-divider';
                    expenseSection.appendChild(divider);

                    const totalItem = document.createElement('div');
                    totalItem.className = 'tooltip-item';

                    const totalLabel = document.createElement('span');
                    totalLabel.textContent = 'Total gastos:';
                    totalLabel.style.fontWeight = 'bold';

                    const totalValue = document.createElement('span');
                    totalValue.className = 'tooltip-value expense-value';
                    totalValue.textContent = formatCurrency(expenseTotal);

                    totalItem.appendChild(totalLabel);
                    totalItem.appendChild(totalValue);
                    expenseSection.appendChild(totalItem);
                }
            }

            tooltip.appendChild(expenseSection);
        }

        return tooltip;
    }


    const generateMonth = (year, month) => {
        const daysInMonth = getDaysInMonth(year, month);
        const firstDay = getFirstDayOfMonth(year, month);

        const monthElement = document.createElement('div');
        monthElement.className = 'month-card';


        const monthHeader = document.createElement('div');
        monthHeader.className = 'month-header';
        monthHeader.textContent = monthNames[month];
        monthElement.appendChild(monthHeader);


        const monthGrid = document.createElement('div');
        monthGrid.className = 'month-grid';


        weekdayNames.forEach(weekday => {
            const weekdayElement = document.createElement('div');
            weekdayElement.className = 'weekday';
            weekdayElement.textContent = weekday;
            monthGrid.appendChild(weekdayElement);
        });


        for (let i = 0; i < firstDay; i++) {
            const emptyDay = document.createElement('div');
            monthGrid.appendChild(emptyDay);
        }


        for (let i = 1; i <= daysInMonth; i++) {
            const dayContainer = document.createElement('div');
            dayContainer.className = 'day-container';


            const hasExpense = expenseDays[month] && expenseDays[month][i];
            const hasIncome = incomeDays[month] && incomeDays[month][i];


            const dayBg = document.createElement('div');
            dayBg.className = 'day-bg';

            if (hasExpense && hasIncome) {
                dayBg.classList.add('split-bg');
            } else if (hasExpense) {
                dayBg.classList.add('expense-bg');
            } else if (hasIncome) {
                dayBg.classList.add('income-bg');
            }

            dayContainer.appendChild(dayBg);

            const dayElement = document.createElement('div');
            dayElement.className = 'day';
            dayElement.textContent = i;


            if (year === today.getFullYear() && month === today.getMonth() && i === today.getDate()) {
                dayElement.classList.add('current-day');
            }

            dayContainer.appendChild(dayElement);


            if (hasExpense || hasIncome) {
                const tooltip = createDayTooltip(month, i);
                dayContainer.appendChild(tooltip);
            }

            monthGrid.appendChild(dayContainer);
        }

        monthElement.appendChild(monthGrid);


        let monthTotal = "Sem dados";
        let expenseCount = 0;
        let incomeCount = 0;

        if (expenseDays[month]) {
            expenseCount = Object.keys(expenseDays[month]).length;
        }

        if (incomeDays[month]) {
            incomeCount = Object.keys(incomeDays[month]).length;
        }

        if (expenseCount > 0 || incomeCount > 0) {
            monthTotal = `${expenseCount} gastos, ${incomeCount} recebimentos`;
        }


        const infoBox = document.createElement('div');
        infoBox.className = 'info-box';
        infoBox.textContent = monthTotal;
        monthElement.appendChild(infoBox);

        return monthElement;
    };


    const renderYear = async (year) => {

        monthsGrid.innerHTML = '';


        currentYearElement.textContent = year;


        const expensesData = await fetchExpensesByMonth();
        processExpenseData(expensesData);


        const additionsData = await fetchYearlyAdditions(year);
        processIncomeData(additionsData);


        for (let i = 0; i < 12; i++) {
            const monthElement = generateMonth(year, i);
            monthsGrid.appendChild(monthElement);
        }
    };


    const initializeApp = async () => {
        await fetchBalance();
        await fetchSalary();
        await renderYear(currentYear);
    };


    initializeApp();


    prevYearButton.addEventListener('click', () => {
        currentYear--;
        renderYear(currentYear);
    });

    nextYearButton.addEventListener('click', () => {
        currentYear++;
        renderYear(currentYear);
    });


    updateSalaryBtn.addEventListener('click', () => {
        salaryModal.style.display = 'block';
    });

    updateSalaryDateBtn.addEventListener('click', () => {
        salaryDateModal.style.display = 'block';
    });

    closeSalaryModal.addEventListener('click', () => {
        salaryModal.style.display = 'none';
    });

    closeSalaryDateModal.addEventListener('click', () => {
        salaryDateModal.style.display = 'none';
    });

    cancelSalaryBtn.addEventListener('click', () => {
        salaryModal.style.display = 'none';
    });

    cancelSalaryDateBtn.addEventListener('click', () => {
        salaryDateModal.style.display = 'none';
    });

    saveSalaryBtn.addEventListener('click', async () => {
        const salaryAmount = document.getElementById('salary-amount').value;
        if (salaryAmount) {
            const success = await updateSalary(salaryAmount);
            if (success) {
                alert('Salário atualizado com sucesso!');
                salaryModal.style.display = 'none';
            } else {
                alert('Erro ao atualizar o salário.');
            }
        } else {
            alert('Por favor, informe um valor válido.');
        }
    });

    saveSalaryDateBtn.addEventListener('click', async () => {
        const salaryDate = document.getElementById('salary-date').value;
        if (salaryDate && salaryDate >= 1 && salaryDate <= 31) {
            const success = await updateSalaryDate(salaryDate);
            if (success) {
                alert('Data de recebimento atualizada com sucesso!');
                salaryDateModal.style.display = 'none';
            } else {
                alert('Erro ao atualizar a data de recebimento.');
            }
        } else {
            alert('Por favor, informe um dia válido (1-31).');
        }
    });


    window.addEventListener('click', (event) => {
        if (event.target === salaryModal) {
            salaryModal.style.display = 'none';
        }
        if (event.target === salaryDateModal) {
            salaryDateModal.style.display = 'none';
        }
    });
});