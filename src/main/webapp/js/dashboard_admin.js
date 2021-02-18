let usuariosData = [];
let usuarioAModificar = {};
//Elementos HTML
const $btnUsuariosActividad = document.getElementById("UsuariosActividad");
const $btnUsuariosHorarios = document.getElementById("UsuariosHorarios");
const $btnUsuarios = document.getElementById("btnUsuarios");
const $btnCrearUsuario = document.getElementById("btn-crear-usuario");

//Eventos
$btnUsuariosActividad.addEventListener("click", cantidadUsuariosPorActividad);
$btnUsuariosHorarios.addEventListener("click", cantidadUsuariosPorHorarios);
$btnUsuarios.addEventListener("click", listaUsuarios);
$btnCrearUsuario.addEventListener("click", crearFomulario);

document.getElementById("accion-edit").addEventListener("click", editarUsuario);
document
  .getElementById("accion-delete")
  .addEventListener("click", eliminarUsuario);

function crearFomulario() {
  clearInformacion();
  $informacion.innerHTML = crearFormularioRegistro("crear");
  const $btn = document.getElementById("sendForm");
  $btn.addEventListener("click", crearUsuario);
}

function haddlerListaUsuarios(event) {
  const element = event.target;
  const modal = document.getElementById("modalAdmin");
  if (element.classList.contains("edit")) {
    clearModal();
    const btnEdit = document.getElementById("accion-edit");
    btnEdit.classList.remove("d-none");
    modal.firstElementChild.classList.add("width-edit-form");
    usuarioAModificar = usuariosData.filter((e) => e.id == element.id)[0];
    modal.querySelector(".modal-title").innerHTML = "Editar Usuario";
    modal.querySelector(".modal-body").innerHTML = crearFormularioRegistro(
      null,
      usuarioAModificar
    );
    // mostrar modal
    $("#modalAdmin").modal();
    // btnEdit.addEventListener("click", () => editarUsuario(usuario.id))
  } else if (element.classList.contains("delete")) {
    clearModal();
    const btnDelete = document.getElementById("accion-delete");
    btnDelete.classList.remove("d-none");
    usuarioAModificar = usuariosData.filter((e) => e.id == element.id)[0];
    modal.querySelector(".modal-title").innerHTML = "Banear Usuario";
    modal.querySelector(".modal-body").innerHTML = `
    <div>
      Seguro que quieres banear a 
      <span class="border-bottom border-danger">${usuarioAModificar.nombres} ${usuarioAModificar.apellidos}</span> 
      que tiene el correo <span class="border-bottom border-danger">${usuarioAModificar.email}</span>
    </div>
    `;
    // mostrar modal
    $("#modalAdmin").modal();
    // btnDelete.addEventListener("click", () => eliminarUsuario(usuario.id))
  }
}

function eliminarUsuario() {
  fetchData(`banearuser?id=${usuarioAModificar.id}`, { method: "PUT" }).then(
    (data) => {
      usuarioAModificar = {};
      swal({
        title: data.titulo,
        text: data.mensaje,
        icon: "success",
      }).then(() => $("#modalAdmin").modal("hide"));
    }
  );
}

async function editarUsuario() {
  const frm = document.getElementById("formulario-general");
  const formulario = new FormData(frm);
  formulario.append("id_usuario", usuarioAModificar.id);
  const config = {
    method: "PUT",
    body: formulario,
    header: { "Content-Type": "application/x-www-form-urlencoded" },
  };
  fetchData("updateuser", config).then((data) => {
    usuarioAModificar = {};
    swal({
      title: data.titulo,
      text: data.mensaje,
      icon: "success",
    }).then(() => $("#modalAdmin").modal("hide"));
  });
}

async function listaUsuarios() {
  const json = await fetchData("usuarios");
  usuariosData = json.usuarios;
  clearInformacion();
  $informacion.innerHTML = createTableHTML(usuariosData, ["editar", "banear"]);
  const $btnTablaUsuarios = document.getElementById("listaUsuarios");
  $btnTablaUsuarios.addEventListener("click", haddlerListaUsuarios);
}

async function cantidadUsuariosPorActividad() {
  const json = await fetchData("usuariosactividades");
  const listaUsuariosActividades = json.lista_usuarios_por_actividad;
  let labels = [];
  let values = [];
  listaUsuariosActividades.forEach((e) => {
    labels.push(e.nombre_actividad);
    values.push(e.cantidad_usuarios);
  });
  clearInformacion();
  createCanvas("lista_usuarios_por_actividad");
  displayGraphics(
    labels,
    values,
    "lista_usuarios_por_actividad",
    "Cantidad Usuarios por Actividad"
  );
}

async function cantidadUsuariosPorHorarios() {
  const json = await fetchData("usuariosporhorarios");
  const listaUsuariosHorarios = json.lista_usuarios_horarios;
  listaUsuariosHorarios.sort(
    (a, b) => b.cantidad_usuarios - a.cantidad_usuarios
  );
  clearInformacion();
  $informacion.innerHTML = createTableHTML(listaUsuariosHorarios);
}

function clearModal() {
  const modal = document.getElementById("modalAdmin");
  const btnEdit = document.getElementById("accion-edit");
  const btnDelete = document.getElementById("accion-delete");
  modal.firstElementChild.classList.remove("width-edit-form");
  modal.querySelector(".modal-title").innerHTML = "";
  modal.querySelector(".modal-body").innerHTML = "";
  // limpiar butones
  btnEdit.classList.add("d-none");
  btnDelete.classList.add("d-none");
}
