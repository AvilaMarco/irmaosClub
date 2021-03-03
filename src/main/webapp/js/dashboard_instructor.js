//al iniciar la pagina
window.onload = () => {
  setInfoUser();
};

//Elementos HTML
const $btnTomarLista = document.getElementById("btn-tomar-lista");

//Eventos
$btnTomarLista.addEventListener("click", manejadorTomarLista);

//varibles globales
let infoClase = {};

//Funciones
/* -- TOMAR LISTA -- */
async function manejadorTomarLista() {
  const data = await fetchData(
    `ultimaclase?id_actividad=${user.actividad.id_actividad}`
  );
  if (data) {
    infoClase = data;
    infoClase.ausentes = infoClase.alumnos.filter(
      (a) => !infoClase.presentes.some((p) => p.id_usuario == a.id_usuario)
    );
    htmlInfoClase("recuperar", traducirDia(infoClase.dia));
    HTMLlistaAsistencia(infoClase.ausentes, infoClase.presentes);
  } else {
    htmlInfoClase("crear");
  }
}

/* (Start) Setear Eventos */
// _ setear eventos
function _btnsTomarLista() {
  const $btnTomarLista = document.getElementById("EnviarAsistencia");
  const $btnTerminarClase = document.getElementById("FinalizarClase");

  $btnTomarLista?.addEventListener("click", tomarLista);
  $btnTerminarClase?.addEventListener("click", finalizarClase);
}

function _btnCrearClase() {
  const btn = document.getElementById("btn-crear-clase");
  btn?.addEventListener("click", () =>
    crearClase(user.actividad.id_actividad, dia)
  );
}

/* (End) Setear Eventos */

/* (Start) Setear Data en Servidor */
async function tomarLista() {
  const presentes = Array.from(
    document.querySelectorAll("input[name*=presente]:checked")
  ).map((e) => e.value);

  const formulario = new FormData();
  formulario.append("id_clase", infoClase.id_clase);
  presentes.forEach((presente) => formulario.append("presentes[]", presente));
  const opcionesFetch = {
    method: "POST",
    body: formulario,
    header: { "Content-Type": "application/x-www-form-urlencoded" },
  };

  const data = await fetchData("tomarlista", opcionesFetch);
  filtrarAlumnos(data);
  HTMLlistaAsistencia(infoClase.ausentes, infoClase.presentes);
}
async function crearClase(id, dia) {
  infoClase = await fetchData(`crearclase?id_actividad=${id}&dia=${dia}`, {
    method: "POST",
  });
  quitarbtnCrearClase();
  HTMLlistaAsistencia(infoClase.alumnos);
}

async function finalizarClase() {
  const data = await fetchData(`terminarclase?id_clase=${infoClase.id_clase}`, {
    method: "PUT",
  });
  //sweet alert
  console.log(data);
}
/* (End) Setear Data en Servidor */

function filtrarAlumnos(data) {
  const listaPresentes = data.presentes.concat(
    infoClase.presentes.map((e) => e.id_usuario.toString())
  );
  infoClase.presentes = [];
  infoClase.ausentes = [];
  infoClase.alumnos.forEach((e) => {
    if (listaPresentes.indexOf(e.id_usuario.toString()) != -1) {
      infoClase.presentes.push(e);
    } else {
      infoClase.ausentes.push(e);
    }
  });
}

/* Crear HTML */
//modificar -> agregar el control de hora
function htmlInfoClase(metodo, day) {
  const dia = diaActual();
  const actividad = user.actividad;
  const diaActividad = actividad.dias.split(", ").find((e) => e == dia);
  let html = "";
  if (diaActividad || metodo == "recuperar") {
    html = `
    <div class="input-group w-content mx-auto my-4 flex-column">
      <div class="input-group-prepend">
        <span class="input-group-text">
          Actividad: <button disabled class="ml-1">${actividad.nombre}</button>
        </span>
        <span class="input-group-text">Dia: 
          <button disabled class="ml-1">${day || diaActividad}</button>
        </span>
        <span class="input-group-text">Hora: 
          <button disabled class="ml-1">${actividad.hora}</button>
        </span>
      </div>
      <div id="div-info-clase">
      ${
        metodo == "crear"
          ? `<button class="btn btn-primary btn-block" id="btn-crear-clase">Crear Clase</button>`
          : `<div class="btn btn-info btn-block">Ultima Clase</div>`
      }
      </div>
    </div>
    <div id="alumnos" class="d-flex flex-equals flex-column flex-md-row"></div>
    `;
  } else {
    html = `
      <div class="jumbotron text-center">
        <h1>Hoy no hay clases!!!!</h1>
      </div>
    `;
  }
  $informacion.innerHTML = html;
  _btnCrearClase();
}

function HTMLlistaAsistencia(alumnos, presentes) {
  const $divAlumnos = document.getElementById("alumnos");
  $divAlumnos.innerHTML = "";
  /* asistencia */
  const $divAsistencia = document.createElement("DIV");
  const titular = {
    titulo: "Asistencia",
    color: "orange",
  };
  $divAsistencia.innerHTML = createTableHTML(alumnos, ["presente"], titular);
  $divAsistencia.innerHTML += `
    <div>
      <button class="btn btn-info" id="EnviarAsistencia">Enviar</button>
      <button class="btn btn-danger" id="FinalizarClase">Finalizar Clase</button>
    </div>`;
  $divAlumnos.appendChild($divAsistencia);
  selectByRow();
  _btnsTomarLista();
  /* Presentes */
  if (presentes) {
    const $divPresentes = document.createElement("DIV");
    const titular = {
      titulo: "Presentes",
      color: "success",
    };
    $divPresentes.innerHTML = createTableHTML(presentes, [], titular);
    $divAlumnos.appendChild($divPresentes);
  }
}

/* Limpiar HTML */
function quitarbtnCrearClase() {
  document.getElementById("div-info-clase").innerHTML = "";
}
//pruebas
async function pruebas() {
  const user = {
    nombre: "marco",
    apellido: "avila",
    arr: [1, 2, 34, 4],
  };
  const formulario = new FormData();
  formulario.append("nombre", "marco");
  formulario.append("apellido", "avila");
  const config = {
    method: "POST",
    body: formulario,
    header: { "Content-Type": "application/x-www-form-urlencoded" },
  };
  const data = await fetchData("pruebas?id=6", config);
  console.log(data);
}
