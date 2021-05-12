const btnLogin = document.getElementById("login");
btnLogin.addEventListener("click", login);

async function login(e) {
  e.preventDefault();
  const formulario = document.getElementById("login-form");

  const correo = document.getElementById("email").value;
  const password = document.getElementById("password").value;
  const correoValido = emailValido(correo);
  if (correoValido || password !== "") {
    const data = await fetchData(`login?email=${correo}&password=${password}`);
    SwalAlert(data).then(() => formulario.reset());
  } else {
    const data = {
      icon: "error",
      title: "Oops...",
      text: "Email o Contraseña invalidos",
    };
    SwalAlert(data);
  }
}
