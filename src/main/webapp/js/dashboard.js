//Elementos HTML
const $informacion = document.getElementById("informacion");
let user = {};

/* Fetch Data */
function fetchData(url, config) {
  return fetch(`../${url}`, config ? config : { method: "GET" })
    .then((data) => (data.redirected ? location.assign(data.url) : data.json()))
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

function SwalAlert(alert) {
  return Swal.fire({
    icon: alert.icon,
    title: alert.title,
    text: alert.text,
  });
}

async function crearUsuario(e) {
  e.preventDefault();
  let inputs = Array.from(document.querySelectorAll("input[required]"));
  if (inputs.some((e) => e.value == "")) {
    Swal.fire({
      icon: "error",
      title: "Oops...",
      text: "Complete todos los campos",
    });
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
  if (page === pageAdmin) {
    SwalAlert(data);
  } else if (page === pageClient) {
    SwalAlert(data).then(() => actividadesFamiliares());
  } else {
    SwalAlert(data).then(() => location.assign("login.html"));
  }
}

async function setInfoUser() {
  user = await fetchData("getinfouser");
}

function createTableHTML(array, camposExtra = [], titular) {
  let header = "";
  let body = "";
  let campos = camposExtra;
  //cambiamos table por el nombre de la tabla
  let id =
    camposExtra?.[0] == "fa-user-edit"
      ? "listaUsuarios"
      : titular?.titulo ?? "table";
  /* Header */
  for (const key in array[0]) {
    campos.push(key);
  }
  campos = campos.filter((e) => e.indexOf("id") !== 0);
  campos.forEach((e) => {
    let thName =
      e == "presente" ||
      e == "pago-registro" ||
      e == "pago" ||
      e == "pago-proceso"
        ? ""
        : e;
    header += `<th class="sticky-top">${thName}</th>`;
  });
  /* Body */
  array.forEach((e) => {
    body += `
      <tr>
        ${campos.reduce((fila, campo) => {
          fila += `<td>`;
          switch (campo) {
            case "fa-user-edit":
            case "fa-user-times":
            case "fa-money-check-alt":
            case "fa-unlock-alt":
              fila += `
                <i id="${e.id}" class="fas ${campo} fa-2x" role="button"></i>`;
              break;
            case "pago":
            case "pago-registro":
            case "pago-proceso":
            case "presente":
            case "actividad deseada":
              fila += `
                <input type="checkbox" class="input-checkbox" 
                  value='${JSON.stringify(e)}' 
                  name="${campo}"
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

async function tablasPago(usuario, isAdmin) {
  const data = await fetchData(
    `checkout?id_usuario=${usuario.id}&isAdmin=${isAdmin}`
  );
  // debugger;
  const tablasActividades = document.createElement("DIV");
  let contenidoTablasHTML = "";
  for (let i = 0; i < data.length; i++) {
    const infoActividad = data[i];
    if (infoActividad.actividades.length > 0) {
      if (infoActividad.titulo == "Procesando Pago") {
        user.pagoTotal = infoActividad.actividades.reduce(
          (acc, e) => acc + e.unitPrice,
          0
        );
      }
      contenidoTablasHTML += createTableHTML(
        infoActividad.actividades,
        infoActividad.checkbox,
        infoActividad
      );
    }
  }
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
    if (e.querySelector("input") != null) {
      e.addEventListener("click", manejadorTabla);
    }
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
