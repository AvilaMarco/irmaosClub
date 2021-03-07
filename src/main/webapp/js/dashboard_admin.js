let usuariosData = [];
let usuarioAModificar = {};
//Elementos HTML
const $btnUsuariosActividad = document.getElementById("UsuariosActividad");
const $btnUsuariosHorarios = document.getElementById("UsuariosHorarios");
const $btnDineroPorMes = document.getElementById("dinero-por-mes");
const $btnUsuarios = document.getElementById("btnUsuarios");
const $btnCrearUsuario = document.getElementById("btn-crear-usuario");
// Acciones para la lista de Usuarios
const btnAccionEditar = document.getElementById("accion-edit");
const btnAccionDelete = document.getElementById("accion-delete");
const btnDatosPago = document.getElementById("accion-pagar");
const btnAccionResetPassword = document.getElementById("accion-reset-password");

//Eventos
$btnUsuariosActividad.addEventListener("click", cantidadUsuariosPorActividad);
$btnUsuariosHorarios.addEventListener("click", cantidadUsuariosPorHorarios);
$btnDineroPorMes.addEventListener("click", traerDineroPorMes);
$btnUsuarios.addEventListener("click", listaUsuarios);
$btnCrearUsuario.addEventListener("click", crearFormularioUsuario);
// Acciones para la lista de Usuarios
btnAccionEditar.addEventListener("click", editarUsuario);
btnAccionDelete.addEventListener("click", eliminarUsuario);
btnDatosPago.addEventListener("click", pagar);
btnAccionResetPassword.addEventListener("click", () =>
  resetPassword(usuarioAModificar.id)
);

/* Lista de Usuarios */
async function listaUsuarios() {
  const json = await fetchData("usuarios");
  usuariosData = json.usuarios;
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
    clearModal();
    MostrarBtnAccion("accion-edit");
    modal.firstElementChild.classList.add("width-edit-form");
    usuarioAModificar = usuariosData.find((e) => e.id == element.id);

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
  } else if (element.classList.contains("fa-money-check-alt")) {
    //pagar actividades de usuario
    clearModal();
    usuarioAModificar = usuariosData.find((e) => e.id == element.id);
    MostrarBtnAccion("accion-pagar");
    const tablasActividades = await tablasPago(usuarioAModificar, true);
    const divBotones = document.createElement("DIV");
    let contenidoBotonesHTML = "";
    //botones
    contenidoBotonesHTML += `
    <button id="marcar-actividades" class="btn btn-info d-none">
      Marcar Todas las Actividades
    </button>
    `;
    // contenidoBotonesHTML += `
    // <button class="btn btn-info" data-toggle="modal" data-target="#modal" id="pagar">
    //   Pagar
    // </button>
    // `;
    divBotones.innerHTML = contenidoBotonesHTML;
    modalTitle.innerHTML = "Registro Pago";

    modalBody.appendChild(tablasActividades);
    modalBody.appendChild(divBotones);

    selectByRow();
    //agregar eventos
    const btnMarcarActividades = document.getElementById("marcar-actividades");
    // const btnDatosPago = document.getElementById("pagar");
    // btnDatosPago.addEventListener("click", pagar);
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
    swal({
      title: data.titulo,
      text: data.mensaje,
      icon: "success",
    }).then(() => $("#modalAdmin").modal("hide"));
  });
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

async function pagar() {
  const inputActividades = Array.from(
    document.querySelectorAll("[name=pago-proceso]")
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
  modal.firstElementChild.classList.remove("width-edit-form");
  modal.querySelector(".modal-title").innerHTML = "";
  modal.querySelector(".modal-body").innerHTML = "";
  // limpiar butones
  btnEdit.classList.add("d-none");
  btnDelete.classList.add("d-none");
  btnPgar.classList.add("d-none");
  btnReset.classList.add("d-none");
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
