const $info = document.getElementById("informacion");
$info.innerHTML = crearFormularioRegistro(null, null, "default");

const $btn = document.getElementById("sendForm");
$btn.addEventListener("click", crearUsuario);
