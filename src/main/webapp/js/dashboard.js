//Elementos HTML
const $informacion = document.getElementById("informacion");
let user = {};

/* Fetch Data */
function fetchData(url, config) {
  return fetch(`../${url}`, config ? config : { method: "GET" })
    .then((data) => data.json())
    .then((json) => json)
    .catch((err) => console.log(err));
}

async function resetPassword(id, password = "1234") {
  const datos = new FormData();
  datos.append("id", id);
  datos.append("password", password);
  const config = {
    method: "PUT",
    body: datos,
    header: { "Content-Type": "application/x-www-form-urlencoded" },
  };
  const data = await fetchData("resetpassword", config);
}

async function crearUsuario(e) {
  e.preventDefault();
  let inputs = Array.from(document.querySelectorAll("input[required]"));
  if (inputs.some((e) => e.value == "")) {
    alert("Complete todos los campos");
    return;
  }
  const frm = document.getElementById("formulario-general");
  const formulario = new FormData(frm);
  const url = location.pathname.split("/");
  const page = url[url.length - 1];
  const pageAdmin = "dashboard_admin.html";
  const pageClient = "dashboard_cliente.html";
  if (page === pageClient) {
    formulario.append("id_titular", user.id);
  }
  const config = {
    method: "POST",
    body: formulario,
    header: { "Content-Type": "application/x-www-form-urlencoded" },
  };
  const data = await fetchData("createuser", config);
  console.log(data);
  //refactor a futuro
  if (page === pageAdmin) {
    swal({
      title: "Usuario Creado",
      text: "El usuario se creo correctamente",
      icon: "success",
    });
  } else if (page === pageClient) {
    swal({
      title: "Usuario Creado",
      text: "El usuario de su Familiar se creo correctamente",
      icon: "success",
    }).then(() => actividadesFamiliares());
  } else {
    swal({
      title: "Usuario Creado",
      text: "El usuario se creo correctamente",
      icon: "success",
    }).then(() => location.assign("login.html"));
  }
}

async function setInfoUser() {
  user = await fetchData("getinfouser");
}

function createTableHTML(array, camposExtra = [], titular) {
  let header = "";
  let body = "";
  let campos = camposExtra;
  let id = camposExtra?.[0] == "editar" ? "listaUsuarios" : "table";
  /* Header */
  for (const key in array[0]) {
    campos.push(key);
  }
  campos = campos.filter((e) => e.indexOf("id") !== 0);
  campos.forEach((e) => {
    let thName =
      e == "presente" || e == "pago-registro" || e == "pago" ? "" : e;
    header += `<th class="sticky-top">${thName}</th>`;
  });
  /* Body */
  array.forEach((e) => {
    body += `
      <tr>
        ${campos.reduce((fila, campo) => {
          fila += `<td>`;
          switch (campo) {
            case "editar":
              fila += `
                <i id="${e.id}" class="fas fa-user-edit fa-2x text-info edit" role="button"></i>`;
              break;
            case "banear":
              fila += `
                <i id="${e.id}" class="fas fa-user-times fa-2x text-danger delete" role="button"></i>`;
              break;
            case "pagar":
              fila += `
                <i id="${e.id}" class="fas fa-money-check-alt fa-2x text-success pagar" role="button"></i>`;
              break;
            case "reset-password":
              fila += `
                  <i id="${e.id}" class="fas fa-unlock-alt fa-2x text-primary reset-password" role="button"></i>`;
              break;
            case "pago":
              fila += `
                <input type="checkbox" class="input-checkbox" 
                  value='${JSON.stringify(e)}' 
                  name="pago"
                >`;
              break;
            case "pago-registro":
              fila += `
                    <input type="checkbox" class="input-checkbox" 
                      value='${JSON.stringify(e)}' 
                      name="pago-registro"
                    >`;
              break;
            case "presente":
              fila += `
                  <input type="checkbox" class="input-checkbox" value="${e.id_usuario}" name="presente">`;
              break;
            case "actividad deseada":
              fila += `
                <input type="checkbox" class="input-checkbox" 
                  value='${JSON.stringify(e)}' 
                  name="actividad"
                >`;
              break;
            default:
              fila += `${e[campo]}`;
          }
          fila += "</td>";
          return fila;
        }, "")}
      </tr>`;
  });

  const table = `
      <table class="table table-hover" id="${id}">
      ${
        titular
          ? `
        <caption class="bg-${titular.color} h5 text-center font-weight-bold text-white m-0">
          ${titular.titulo}
        </caption>
        `
          : ""
      }
        <thead class="thead-dark">
          <tr>
            ${header}
          </tr>
        </thead>
        <tbody id="select-by-row">
          ${body}
        </tbody>
      </table>
    `;
  return table;
}

function marcarActividades() {
  console.log("marcar actividades");
}

async function tablasPago(usuario) {
  const data = await fetchData(`checkout?id_usuario=${usuario.id}`);
  const tablasActividades = document.createElement("DIV");
  let contenidoTablasHTML = "";
  //tablas actividades
  const titular2 = {
    titulo: "Actividades - Pago Mensual",
    color: "warning",
  };
  contenidoTablasHTML += createTableHTML(data.itemsMensual, ["pago"], titular2);
  const titular3 = {
    titulo: "Actvidades - Pago Registro",
    color: "info",
  };
  contenidoTablasHTML += createTableHTML(
    data.itemsRegistro,
    ["pago-registro"],
    titular3
  );
  tablasActividades.innerHTML = contenidoTablasHTML;
  return tablasActividades;
}

function actividadesMarcadas() {
  const $inputaData = document.querySelectorAll("input[name=pago]:checked");
  let actividadesPago = Array.from($inputaData).map((e) => JSON.parse(e.value));
  const $inputaDataDeuda = document.querySelectorAll(
    "input[name=pago-registro]:checked"
  );
  let actividadesPagoDeuda = Array.from($inputaDataDeuda).map((e) =>
    JSON.parse(e.value)
  );
  const actividades = {
    actividadesRegistro: actividadesPagoDeuda,
    actividades: actividadesPago,
  };
  return actividades;
}

function selectByRow() {
  const $tablas = Array.from(document.querySelectorAll("#select-by-row"));
  $tablas.forEach((e) => {
    e.addEventListener("click", manejadorTabla);
  });
}

function manejadorTabla(event) {
  const element = event.target;
  const row = element.parentNode;
  row.classList.toggle("table-active");
  const input = row.querySelector("input");
  input.checked = !input.checked;
}

function clearInformacion() {
  $informacion.innerHTML = "";
  $informacion.classList = [];
}

function getEdad(fechaNacimiento) {
  const nacimiento = new Date(fechaNacimiento);
  const time = Date.now();
  const milisegundos = time - nacimiento.getTime();
  const segundos = milisegundos / 1000;
  const minutos = segundos / 60;
  const horas = minutos / 60;
  const dias = horas / 24;
  const años = dias / 365;
  const edad = Math.floor(años);
  return edad;
}

function diaActual() {
  let numeroDia = new Date().getDay();
  let dia = "";
  switch (numeroDia) {
    case 1:
      dia = "lunes";
      break;
    case 2:
      dia = "martes";
      break;
    case 3:
      dia = "miercoles";
      break;
    case 4:
      dia = "jueves";
      break;
    case 5:
      dia = "viernes";
      break;
    case 6:
      dia = "sabado";
      break;
    default:
      dia = "domingo";
  }
  return dia;
}

function traducirDia(day) {
  let dia = "";
  switch (day) {
    case "Monday":
      dia = "lunes";
      break;
    case "Tuesday":
      dia = "martes";
      break;
    case "Wednesday":
      dia = "miercoles";
      break;
    case "Thursday":
      dia = "jueves";
      break;
    case "Friday":
      dia = "viernes";
      break;
    case "Saturday":
      dia = "sabado";
      break;
    default:
      dia = "domingo";
  }
  return dia;
}

/* IMPLEMENTAR A FUTURO, POR AHORA NO SIRVE */
let idClase;

function usuariosPagos() {
  const bodyTable = document.getElementById("usuarios_pagos");
  fetch("../usuariospagos")
    .then((data) => data.json())
    .then((json) => {
      const listaUsuariosPagos = json.lista_usuarios_pagos;
      for (let i = 0; i < listaUsuariosPagos.length; i++) {
        bodyTable.innerHTML += `
                                        <tr>
                                            <td>${
                                              listaUsuariosPagos[i].apellidos
                                            }</td>
                                            <td>${
                                              listaUsuariosPagos[i].nombres
                                            }</td>
                                            <td>${
                                              listaUsuariosPagos[i].email
                                            }</td>
                                            <td>${
                                              listaUsuariosPagos[i].tiene_pago
                                                ? "Si"
                                                : "No"
                                            }</td>
                                            <td>${
                                              listaUsuariosPagos[i].fecha_pago
                                                ? listaUsuariosPagos[i]
                                                    .fecha_pago
                                                : "No Pago"
                                            }</td>
                                        </tr>
                                    `;
      }
    });
}
