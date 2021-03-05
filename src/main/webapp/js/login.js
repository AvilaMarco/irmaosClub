const btnLogin = document.getElementById("login");
btnLogin.addEventListener("click", login);

async function login(e) {
  e.preventDefault();
  const formulario = document.getElementById("login-form");
  const exp = /^[a-z0-9!#$%&'*+/=?^_`{|}~-]+(?:\.[a-z0-9!#$%&'*+/=?^_`{|}~-]+)*@(?:[a-z0-9](?:[a-z0-9-]*[a-z0-9])?\.)+[a-z0-9](?:[a-z0-9-]*[a-z0-9])?$/;
  const correo = document.getElementById("email").value;
  console.log(exp.test(correo));
  const password = document.getElementById("password").value;
  const data = await fetchData(`login?email=${correo}&password=${password}`);
  SwalAlert(data).then(() => formulario.reset());
}
