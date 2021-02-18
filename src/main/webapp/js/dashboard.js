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

async function crearUsuario(e) {
  e.preventDefault();
  const frm = document.getElementById("formulario-general");
  const formulario = new FormData(frm);
  const config = {
    method: "POST",
    body: formulario,
    header: { "Content-Type": "application/x-www-form-urlencoded" },
  };
  const data = await fetchData("createuser", config);
  console.log(data);
  const url = location.pathname.split("/");
  const page = url[url.length - 1];
  const pageAdmin = "dashboard_admin.html";
  if (page === pageAdmin) {
    swal({
      title: "Usuario Creado",
      text: "El usuario se creo correctamente",
      icon: "success",
    });
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

function createTableHTML(array, camposExtra = []) {
  let header = "";
  let body = "";
  let campos = camposExtra;
  let id = camposExtra?.[0] == "editar" ? "listaUsuarios" : "";
  for (const key in array[0]) {
    campos.push(key);
  }
  campos.forEach((e) => (header += `<th>${e}</th>`));
  array.forEach((e) => {
    body += `
      <tr>
        ${campos.reduce((fila, campo) => {
          switch (campo) {
            case "editar":
              fila += `
              <td>
                <i id="${e.id}" class="fas fa-user-edit fa-2x text-info edit" role="button"></i>
              </td>`;
              break;
            case "banear":
              fila += `
              <td>
                <i id="${e.id}" class="fas fa-user-times fa-2x text-danger delete" role="button"></i>
              </td>`;
              break;
            case "presente":
              fila += `
                <td>
                  <input type="checkbox" value="${e.id_usuario}" name="presente">
                </td>`;
              break;
            case "actividad deseada":
              fila += `
                <td>
                  <input type="checkbox" value='${JSON.stringify(
                    e
                  )}' name="actividad">
                </td>`;
              break;
            default:
              fila += `<td>${e[campo]}</td>`;
          }
          return fila;
        }, "")}
      </tr>`;
  });
  const table = `
      <table class="table" id="${id}">
        <thead>
          <tr>
            ${header}
          </tr>
        </thead>
        <tbody>
          ${body}
        </tbody>
      </table>
    `;
  return table;
}

function clearInformacion() {
  $informacion.innerHTML = "";
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
