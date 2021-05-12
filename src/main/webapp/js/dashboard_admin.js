let usuariosData = [];
let usuarioAModificar = {};
//Elementos HTML
const $btnUsuariosActividad = document.getElementById("UsuariosActividad");
const $btnUsuariosHorarios = document.getElementById("UsuariosHorarios");
const $btnDineroPorMes = document.getElementById("dinero-por-mes");
const $btnUsuarios = document.getElementById("btnUsuarios");
const $btnCrearUsuario = document.getElementById("btn-crear-usuario");
const $btnListaActividades = document.getElementById("btn-lista-actividades");
// Acciones para la lista de Usuarios
const btnAccionEditar = document.getElementById("accion-edit");
const btnAccionDelete = document.getElementById("accion-delete");
const btnDatosPago = document.getElementById("accion-pagar");
const btnAccionResetPassword = document.getElementById("accion-reset-password");
// Acciones Crud actividades
const $btnCrearActividad = document.getElementById("btn-crear-actividad");
const $btnEditarActividad = document.getElementById("accion-edit-actividad");
//Eventos
$btnUsuariosActividad.addEventListener("click", cantidadUsuariosPorActividad);
$btnUsuariosHorarios.addEventListener("click", cantidadUsuariosPorHorarios);
$btnDineroPorMes.addEventListener("click", traerDineroPorMes);
$btnUsuarios.addEventListener("click", listaUsuarios);
$btnCrearUsuario.addEventListener("click", crearFormularioUsuario);
$btnListaActividades.addEventListener("click", mostrarListaActividades);
// Acciones Crud Actividades
$btnCrearActividad.addEventListener("click", mostrarFormularioActividad);
$btnEditarActividad.addEventListener("click", editarActividad);
// Acciones para la lista de Usuarios
btnAccionEditar.addEventListener("click", editarUsuario);
btnAccionDelete.addEventListener("click", eliminarUsuario);
btnDatosPago.addEventListener("click", pagar);
btnAccionResetPassword.addEventListener("click", () =>
  resetPassword(usuarioAModificar.id)
);

/* CRUD DE ACTIVIDADES */
async function editarActividad() {
  const formulario = new FormData();
  formulario.append("actividad", JSON.stringify(obtenerDatos()));
  const config = {
    method: "PUT",
    body: formulario,
    header: { "Content-Type": "application/x-www-form-urlencoded" },
  };
  const data = await fetchData("/crudactividades/editActividad", config);
  SwalAlert(data).then(() => {
    $("#modalAdmin").modal("hide");
    mostrarListaActividades();
  });
}

async function mostrarFormularioActividad() {
  clearInformacion();
  const actividades = await fetchData("/crudactividades/listaActividades");
  $informacion.innerHTML = formularioCrearActividad();
  setToggleActiveDays();
  setToggleCreateAcitividad();
  setOptionActividades(actividades);
  setEventCrearActividad(crearActividad);
}

async function crearActividad() {
  const actividad = obtenerActividad();
  if ("string" === typeof actividad) {
    errorAlert(actividad);
  } else {
    const formulario = new FormData();
    formulario.append("actividad", JSON.stringify(actividad));
    const config = {
      method: "POST",
      body: formulario,
      header: { "Content-Type": "application/x-www-form-urlencoded" },
    };
    const data = await fetchData("/crudactividades/creaarActividad", config);
    SwalAlert(data);
  }
}

async function mostrarListaActividades() {
  const actividades = await fetchData("/crudactividades/listaActividadesData");
  clearInformacion();
  $informacion.innerHTML = createTableHTML(actividades, ["edit"], {
    titulo: "Lista Actividades",
    color: "info",
  });
  const tabla = document.getElementById("Lista Actividades");
  tabla.addEventListener("click", handlerCrudActividades);
}

async function handlerCrudActividades(e) {
  const element = e.target;
  const modal = document.getElementById("modalAdmin");
  const modalTitle = document.querySelector(".modal-title");
  const modalBody = document.querySelector(".modal-body");
  if (element.classList.contains("fa-edit")) {
    clearModal();
    MostrarBtnAccion("accion-edit-actividad");
    const actividadData = JSON.parse(element.dataset.actividad);
    const instructores = await fetchData("/crudactividades/listaInstructores");
    modal.firstElementChild.classList.add("width-edit-form");
    modalTitle.innerHTML = "Crud Actividades";
    modalBody.innerHTML = formEditarActividad(actividadData, instructores);
    setToggleActiveDays();
    // mostrar modal
    $("#modalAdmin").modal();
  }
}

/* Lista de Usuarios */
async function listaUsuarios() {
  const json = await fetchData("usuarios");
  usuariosData = json.usuarios;
  let ordenTitularFamiliar = [];
  usuariosData.forEach((user) => {
    if (ordenTitularFamiliar.findIndex((e) => e.id === user.id) === -1) {
      ordenTitularFamiliar.push(user);
    }

    const familiares = usuariosData.filter((f) => f.id_titular === user.id);
    ordenTitularFamiliar = ordenTitularFamiliar.concat(familiares);
  });
  usuariosData = ordenTitularFamiliar;
  clearInformacion();
  $informacion.innerHTML = createTableHTML(usuariosData, [
    "fa-user-edit",
    "fa-user-times",
    "fa-money-check-alt",
    "fa-unlock-alt",
  ]);
  const $btnTablaUsuarios = document.getElementById("listaUsuarios");
  $btnTablaUsuarios.addEventListener("click", haddlerListaUsuarios);
}

function crearFormularioUsuario() {
  clearInformacion();
  $informacion.innerHTML = crearFormularioRegistro("crear");
  const $btn = document.getElementById("sendForm");
  $btn.addEventListener("click", crearUsuario);
}

/* Graficos Usuarios */
async function traerDineroPorMes() {
  const dineroMensual = await fetchData("dineropormes");
  const ordenarDineroMensual = dineroMensual.reverse();
  const values = ordenarDineroMensual.map((e) => e.dinero_total);
  const labels = ordenarDineroMensual.map((e) => mesAño(e));
  clearInformacion();
  createCanvas("canvas-dinero-por-mes");
  displayGraphics(labels, values, "canvas-dinero-por-mes", "Dinero por Mes");
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

async function haddlerListaUsuarios(event) {
  const element = event.target;
  const modal = document.getElementById("modalAdmin");
  const modalTitle = document.querySelector(".modal-title");
  const modalBody = document.querySelector(".modal-body");
  if (element.classList.contains("fa-user-edit")) {
    //editar usuario
    MostrarBtnAccion("accion-edit");
    usuarioAModificar = usuariosData.find((e) => e.id == element.id);
    clearModal();
    modal.firstElementChild.classList.add("width-edit-form");
    modalTitle.innerHTML = "Editar Usuario";
    modalBody.innerHTML = crearFormularioRegistro(null, usuarioAModificar);
    // mostrar modal
    $("#modalAdmin").modal();
  } else if (element.classList.contains("fa-user-times")) {
    //banear usuario
    clearModal();
    MostrarBtnAccion("accion-delete");
    usuarioAModificar = usuariosData.find((e) => e.id == element.id);

    modalTitle.innerHTML = "Banear Usuario";
    modalBody.innerHTML = `
    <div>
      Seguro que quieres banear a 
      <span class="border-bottom border-danger">${usuarioAModificar.nombres} ${usuarioAModificar.apellidos}</span> 
      que tiene el correo <span class="border-bottom border-danger">${usuarioAModificar.email}</span>
    </div>
    `;
    // mostrar modal
    $("#modalAdmin").modal();
  } else if (
    element.classList.contains("fa-money-check-alt") &&
    !element.classList.contains("text-secondary")
  ) {
    //pagar actividades de usuario
    clearModal();
    usuarioAModificar = usuariosData.find((e) => e.id == element.id);
    const tablasActividades = await tablasPago(usuarioAModificar, true);
    const divBotones = document.createElement("DIV");
    let contenidoBotonesHTML = "";
    //botones
    contenidoBotonesHTML += `
    <button id="marcar-actividades" class="btn btn-info d-none">
      Marcar Todas las Actividades
    </button>
    `;
    divBotones.innerHTML = contenidoBotonesHTML;
    modalTitle.innerHTML = "Registro Pago";

    modalBody.appendChild(tablasActividades);
    modalBody.appendChild(divBotones);

    if (document.getElementById("Procesando Pago") != null) {
      MostrarBtnAccion("accion-pagar");
    }

    selectByRow();
    //agregar eventos
    const btnMarcarActividades = document.getElementById("marcar-actividades");
    btnMarcarActividades.addEventListener("click", marcarActividades);

    // mostrar modal
    $("#modalAdmin").modal();
  } else if (element.classList.contains("fa-unlock-alt")) {
    //reset password user
    clearModal();
    MostrarBtnAccion("accion-reset-password");
    usuarioAModificar = usuariosData.find((e) => e.id == element.id);
    modalTitle.innerHTML = "Reset Password";
    modalBody.innerHTML = `
    <div>
      Seguro que quieres resetear la contraseña del usuario 
      <span class="border-bottom border-danger">${usuarioAModificar.nombres} ${usuarioAModificar.apellidos}</span> 
      que tiene el correo <span class="border-bottom border-danger">${usuarioAModificar.email}</span>
      <br>
      La nueva contraseña sera: 1234
    </div>
    `;
    // mostrar modal
    $("#modalAdmin").modal();
  }
}

/* Acciones Administrador */
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
    SwalAlert(data).then(() => $("#modalAdmin").modal("hide"));
  });
}

function eliminarUsuario() {
  fetchData(`banearuser?id=${usuarioAModificar.id}`, { method: "PUT" }).then(
    (data) => {
      usuarioAModificar = {};
      SwalAlert(data).then(() => $("#modalAdmin").modal("hide"));
    }
  );
}

async function pagar() {
  const inputActividades = Array.from(
    document.querySelectorAll("[name=pago-proceso]:checked")
  );
  const actividades = inputActividades.map((e) => JSON.parse(e.value));
  const formulario = new FormData();
  formulario.append("actividades", JSON.stringify(actividades));
  formulario.append("id_usuario", usuarioAModificar.id);
  const config = {
    method: "POST",
    body: formulario,
    header: { "Content-Type": "application/x-www-form-urlencoded" },
  };
  const data = await fetchData("registropago", config);
  SwalAlert(data).then(() => {
    $("#modalAdmin").modal("hide");
  });
}

function clearModal() {
  const modal = document.getElementById("modalAdmin");
  const btnEdit = document.getElementById("accion-edit");
  const btnDelete = document.getElementById("accion-delete");
  const btnPgar = document.getElementById("accion-pagar");
  const btnReset = document.getElementById("accion-reset-password");
  const btnEditActividad = document.getElementById("accion-edit-actividad");
  modal.firstElementChild.classList.remove("width-edit-form");
  modal.querySelector(".modal-title").innerHTML = "";
  modal.querySelector(".modal-body").innerHTML = "";
  // limpiar butones
  btnEdit.classList.add("d-none");
  btnDelete.classList.add("d-none");
  btnPgar.classList.add("d-none");
  btnReset.classList.add("d-none");
  btnEditActividad.classList.add("d-none");
}

function MostrarBtnAccion(btn) {
  document.getElementById(btn).classList.remove("d-none");
}

function mesAño(date) {
  let mes = "";
  switch (date.month) {
    case 1:
      mes = "Ene";
      break;
    case 2:
      mes = "Feb";
      break;
    case 3:
      mes = "Mar";
      break;
    case 4:
      mes = "Abr";
      break;
    case 5:
      mes = "May";
      break;
    case 6:
      mes = "Jun";
      break;
    case 7:
      mes = "Jul";
      break;
    case 8:
      mes = "Ago";
      break;
    case 9:
      mes = "Sep";
      break;
    case 10:
      mes = "Oct";
      break;
    case 11:
      mes = "Nov";
      break;
    default:
      mes = "Dic";
  }
  return `${mes} - ${date.year}`;
}
