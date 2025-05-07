document.addEventListener('DOMContentLoaded', function() {
    let expenseCounter = 1;

    document.getElementById('addExpenseBtn').addEventListener('click', function() {
        const expensesContainer = document.getElementById('expensesContainer');

        const expenseTemplate = `
            <div class="expense-container">
                <div class="expense-header">
                    <div class="expense-title">Despesa Recorrente #${expenseCounter + 1}</div>
                    <div class="expense-actions">
                        <button type="button" class="btn-remove-expense">✕</button>
                    </div>
                </div>
                <div class="expense-form">
                    <div class="form-group">
                        <label for="expenseName${expenseCounter}">Nome da Despesa <span class="required-field">*</span></label>
                        <input type="text" id="expenseName${expenseCounter}" required>
                    </div>

                    <div class="form-group">
                        <label for="expenseAmount${expenseCounter}">Valor <span class="required-field">*</span></label>
                        <input type="number" id="expenseAmount${expenseCounter}" min="0" step="0.01" required>
                    </div>

                    <div class="form-group">
                        <label for="expenseDate${expenseCounter}">Data de Vencimento <span class="required-field">*</span></label>
                        <input type="date" id="expenseDate${expenseCounter}" required>
                    </div>

                    <div class="form-group">
                        <label for="expenseCategory${expenseCounter}">Categoria</label>
                        <select id="expenseCategory${expenseCounter}">
                        <option value="HOUSING">Casa</option>
                        <option value="FOOD">Alimentação</option>
                        <option value="TRANSPORT">Transporte</option>
                        <option value="HEALTH">Saúde</option>
                        <option value="ENTERTAINMENT">Lazer</option>
                        <option value="EDUCATION">Educação</option>
                        <option value="UTILITIES">Serviços</option>
                        <option value="PERSONAL_CARE">Cuidados pessoais</option>
                        <option value="INSURANCE">Seguro</option>
                        <option value="SAVINGS">Poupança</option>
                        <option value="OTHER">Outros</option>
                    </select>
                    </div>

                    <div class="form-group expense-form-full">
                        <label for="expenseDescription${expenseCounter}">Descrição</label>
                        <input type="text" id="expenseDescription${expenseCounter}">
                    </div>

                    <div class="form-group expense-form-full checkbox-group">
                        <input type="checkbox" id="isRecurrent${expenseCounter}" checked disabled>
                        <label for="isRecurrent${expenseCounter}">Despesa Recorrente</label>
                    </div>
                    <p class="recurrent-note expense-form-full">Esta despesa será automaticamente registrada todo mês no dia especificado.</p>
                </div>
            </div>
        `;

        const tempDiv = document.createElement('div');
        tempDiv.innerHTML = expenseTemplate;
        expensesContainer.appendChild(tempDiv.firstElementChild);

        const removeButtons = document.querySelectorAll('.btn-remove-expense');
        removeButtons.forEach(button => {
            button.addEventListener('click', function() {
                this.closest('.expense-container').remove();
                updateExpenseTitles();
            });
        });

        expenseCounter++;
        updateExpenseTitles();
    });

    function updateExpenseTitles() {
        const expenseContainers = document.querySelectorAll('.expense-container');
        expenseContainers.forEach((container, index) => {
            const titleElement = container.querySelector('.expense-title');
            titleElement.textContent = `Despesa Recorrente #${index + 1}`;

            const removeButton = container.querySelector('.btn-remove-expense');
            if (expenseContainers.length > 1) {
                removeButton.style.display = 'block';
            } else {
                removeButton.style.display = 'none';
            }
        });
    }

    document.getElementById('firstRegistryForm').addEventListener('submit', function(e) {
        e.preventDefault();

        const currentBalance = document.getElementById('currentBalance').value;
        const monthlySalary = document.getElementById('monthlySalary').value;
        const salaryDate = document.getElementById('salaryDate').value;

        const expenses = [];
        const expenseContainers = document.querySelectorAll('.expense-container');

        expenseContainers.forEach((container, index) => {
            // Get the date value and convert it to LocalDateTime format
            const dateInput = document.getElementById(`expenseDate${index}`).value;
            // Add time (00:00:00) to the date to make it a proper LocalDateTime
            const formattedDate = dateInput ? `${dateInput}T00:00:00` : null;

            const expense = {
                expenseName: document.getElementById(`expenseName${index}`).value,
                expenseAmount: document.getElementById(`expenseAmount${index}`).value,
                expenseDate: formattedDate,
                expenseCategory: document.getElementById(`expenseCategory${index}`).value,
                description: document.getElementById(`expenseDescription${index}`).value,
                isRecurrent: true
            };

            expenses.push(expense);
        });

        const formData = {
            currentBalance: currentBalance,
            monthlySalary: monthlySalary,
            salaryDate: parseInt(salaryDate),
            expenses: expenses
        };

        console.log('Sending data:', JSON.stringify(formData));

        fetch('/api/expense/first/registry', {
            method: 'POST',
            headers: {
                'Content-Type': 'application/json'
            },
            body: JSON.stringify(formData)
        })
            .then(response => {
                if (response.ok) {
                    document.querySelector('.popup-overlay').style.display = 'none';
                    window.location.reload();
                } else {
                    response.json().then(errorData => {
                        console.error('Server error:', errorData);
                        alert('Ocorreu um erro ao salvar os dados: ' + (errorData.message || 'Por favor, tente novamente.'));
                    }).catch(() => {
                        alert('Ocorreu um erro ao salvar os dados. Por favor, tente novamente.');
                    });
                }
            })
            .catch(error => {
                console.error('Erro:', error);
                alert('Ocorreu um erro ao salvar os dados. Por favor, tente novamente.');
            });
    });
});