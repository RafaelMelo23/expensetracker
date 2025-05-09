document.getElementById('registrationForm').addEventListener('submit', function(event) {
    event.preventDefault();

    const form = event.target;
    const data = {
        firstName: form.firstName.value,
        lastName: form.lastName.value,
        email: form.email.value,
        password: form.password.value
    };


    document.querySelectorAll('.error').forEach(function(error) {
        error.textContent = '';
    });
    document.getElementById('responseMessage').textContent = '';
    document.getElementById('responseMessage').className = '';


    let isValid = true;
    if (!data.firstName || data.firstName.length > 30) {
        document.getElementById('firstNameError').textContent = 'Nome inválido.';
        isValid = false;
    }
    if (!data.lastName || data.lastName.length > 50) {
        document.getElementById('lastNameError').textContent = 'Sobrenome inválido..';
        isValid = false;
    }
    if (!data.email || !/\S+@\S+\.\S+/.test(data.email)) {
        document.getElementById('emailError').textContent = 'E-mail inválido.';
        isValid = false;
    }
    if (!data.password || data.password.length < 8 || data.password.length > 72) {
        document.getElementById('passwordError').textContent = 'A senha deve ser entre 8 e 72 caracteres.';
        isValid = false;
    }

    const confirmPassword = document.getElementById('confirmPassword').value;
    if (data.password !== confirmPassword) {
        document.getElementById('confirmPasswordError').textContent = 'As senhas não coincidem.';
        isValid = false;
    }

    if (!isValid) return;


    fetch('/api/user/register', {
        method: 'POST',
        headers: {
            'Content-Type': 'application/json'
        },
        body: JSON.stringify(data)
    })
        .then(response => {
            if (response.status === 201) {
                document.getElementById('responseMessage').textContent = 'Cadastro realizado com sucesso! Você já pode fazer login.';
                document.getElementById('responseMessage').classList.add('success');
                form.reset();
            } else {
                response.json().then(error => {
                    document.getElementById('responseMessage').textContent = 'Erro no cadastro.'
                });
            }
        })
        .catch(error => {
            document.getElementById('responseMessage').textContent = 'Erro ao cadastrar o usuário'
        });
});