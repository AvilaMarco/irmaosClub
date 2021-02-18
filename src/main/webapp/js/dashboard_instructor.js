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
async function manejadorTomarLista() {
  const data = await fetchData(
    `ultimaclase?id_actividad=${user.actividad.id_actividad}`
  );
  if (data) {
    infoClase = data;
    infoClase.ausentes = infoClase.alumnos.filter(
      (a) => !infoClase.presentes.some((p) => p.id_usuario == a.id_usuario)
    );
    htmlInfoClase("recuperar");
    HTMLlistaAsistencia(infoClase.ausentes, infoClase.presentes);
  } else {
    htmlInfoClase("crear");
  }
}

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
  console.log(data);
}
/* Crear HTML */
//modificar -> agregar el control de hora
function htmlInfoClase(metodo) {
  const dia = diaActual();
  const actividad = user.actividad;
  const diaActividad = actividad.dias.split(", ").filter((e) => e == dia)[0];
  if (diaActividad || metodo == "recuperar") {
    $informacion.innerHTML = `
    <div class="input-group my-4 justify-content-center">
      <div class="input-group-prepend">
        <span class="input-group-text">Actividad: <button disabled class="ml-1">${actividad.nombre}</button></span>
        <span class="input-group-text">Dia: <button disabled class="ml-1">${actividad.dia}</button></span>
        <span class="input-group-text">Hora: <button disabled class="ml-1">${actividad.hora}</button></span>
      </div>
      <div class="input-group-append" id="div-crear-clase"></div>
    </div>
    <div id="asistencia"></div>
    <div id="presentes"></div>
    `;
    if (metodo == "crear") {
      btnCrearClase(diaActividad);
    }
  } else {
    $informacion.innerHTML = `
      <div class="jumbotron text-center">
        <h1>Hoy no hay clases!!!!</h1>
      </div>
    `;
  }
}

function HTMLlistaAsistencia(alumnos, presentes) {
  const $divAsistencia = $informacion.querySelector("#asistencia");
  $divAsistencia.innerHTML = createTableHTML(alumnos, ["presente"]);
  crearBtnTomarLista();
  if (presentes) {
    const $divPresentes = $informacion.querySelector("#presentes");
    $divPresentes.innerHTML = createTableHTML(presentes);
  }
}

function btnCrearClase(dia) {
  const $contenedor = document.getElementById("div-crear-clase");
  contenedor.innerHTML = `<button class="btn btn-primary" type="button" id="btn-crear-clase">Crear Clase</button>`;

  const $btnCrearClase = document.getElementById("btn-crear-clase");
  $btnCrearClase.addEventListener("click", () =>
    crearClase(user.actividad.id_actividad, dia)
  );
}

function crearBtnTomarLista() {
  $informacion.querySelector("#asistencia").innerHTML += `
      <div>
        <button class="btn btn-info" id="EnviarAsistencia">Enviar</button>
        <button class="btn btn-danger" id="FinalizarClase">Finalizar Clase</button>
      </div>`;
  const $btnTomarLista = document.getElementById("EnviarAsistencia");
  const $btnTerminarClase = document.getElementById("FinalizarClase");

  $btnTomarLista.addEventListener("click", tomarLista);
  $btnTerminarClase.addEventListener("click", finalizarClase);
}

/* Limpiar HTML */
function quitarbtnCrearClase() {
  document.getElementById("div-crear-clase").innerHTML = "";
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
