function formularioCrearActividad() {
  const dias = diasSemana([]);
  return `
    <div id="crear-actividad">
        <div>
            <h3>Actividad</h3>
            <div id="create-actividad">
                <input type="text" name="nombre" placeholder="Nombre" class="form-control"/>
                <input type="number" name="precio" placeholder="Precio" class="form-control"/>
            </div>
            <div id="select-actividad" class="d-none">
                <select name="actividad" id="lista-actividades" class="form-control">
                </select>
            </div>
            <div class="custom-control custom-switch">
                <input type="checkbox" class="custom-control-input" id="custom-toggle">
                <label id="toggle-actividad" class="custom-control-label" for="custom-toggle">
                    Elegir Actividad
                </label>
            </div>
        </div>
        <div>
            <h3>Horario</h3>
            <input
            type="number"
            name="hora"
            placeholder="Hora"
            min="8"
            max="24"
            class="form-control"
            />
        </div>
        <div>
            <h3>Dias</h3>
            <div class="content-semana">
                ${dias}
            </div>
        </div>
        <div>
            <button id="submit-crear-actividad" class="btn btn-info">Crear Actividad</button>
        </div>
    </div>
  `;
}

function diasSemana(arrDias) {
  const dias = [
    "lunes",
    "martes",
    "miercoles",
    "jueves",
    "viernes",
    "sabado",
    "domingo",
  ];
  const abreviarDia = (dia) => (dia.charAt(0) + dia.charAt(1)).toUpperCase();
  const opcionesHTML = dias.reduce((opciones, dia) => {
    const isActive = arrDias.indexOf(dia) >= 0;
    const active = isActive ? "content-day-active" : "";
    const checked = isActive ? "checked='true'" : "";
    return (
      opciones +
      `
    <label class="card-dia ${active}">
        <span class="day">
          ${abreviarDia(dia)}
        </span>
        <input id="${dia}" type="checkbox" name="dias" value="${dia}" ${checked}/>
    </label>`
    );
  }, "");
  return opcionesHTML;
}

function setToggleActiveDays() {
  const daysContent = Array.from(
    document.querySelectorAll(".content-semana input")
  );
  daysContent.forEach((e) => e.addEventListener("change", toggleActiveDay));
}

function setEventCrearActividad(funcion) {
  const btn = document.getElementById("submit-crear-actividad");
  btn.addEventListener("click", funcion);
}

function setToggleCreateAcitividad() {
  const btnToggleActividad = document.querySelector("#toggle-actividad");
  btnToggleActividad.addEventListener("click", toggleActiveActividad);
}

function setOptionActividades(actividades) {
  const select = document.querySelector("#lista-actividades");
  select.innerHTML = actividades.reduce(
    (opciones, actividad) =>
      opciones +
      `<option value="${actividad.id_actividad}">${actividad.nombre}</option>`,
    ""
  );
}

function obtenerActividad() {
  const $data = document.querySelector("#crear-actividad");
  const esSeleccionActividad = $data.querySelector("#custom-toggle").checked;
  const idActividad = $data.querySelector("#lista-actividades").value;
  const precio = $data.querySelector("[name=precio]").value;
  const nombre = $data.querySelector("[name=nombre]").value;
  const hora = $data.querySelector("[name=hora]").value;
  if (!esSeleccionActividad) {
    if (precio === "" || nombre === "")
      return "Complete los datos de la Actividad";
  }
  if (hora === "") return "Seleccione un Horario";
  const inputsCheckbox = Array.from($data.querySelectorAll("[name=dias]"));
  if (inputsCheckbox.every((e) => !e.checked)) return "Seleccione un dia";

  const actividad = {
    idActividad: 0,
    nombre: "",
    precio: 0,
  };
  if (esSeleccionActividad) {
    actividad.idActividad = parseInt(idActividad);
  } else {
    actividad.nombre = nombre;
    actividad.precio = parseInt(precio);
  }
  actividad.hora = parseInt(hora);
  const inputDias = Array.from($data.querySelectorAll("[name=dias]:checked"));
  const dias = inputDias.reduce((dias, inputDia) => {
    dias.push(inputDia.value);
    return dias;
  }, []);

  actividad.dias = dias;
  return actividad;
}

function toggleActiveActividad() {
  const selectActividad = document.getElementById("select-actividad");
  const createActividad = document.getElementById("create-actividad");
  const btn = document.getElementById("crear-actividad");
  const toggle = document.getElementById("custom-toggle");
  selectActividad.classList.toggle("d-none");
  createActividad.classList.toggle("d-none");
  btn.innerText = !toggle.checked ? "Crear Horario" : "Crear Actividad";
}

function toggleActiveDay(e) {
  const input = e.target;
  const label = input.parentElement;
  // label.classList.toggle("content-day-active",input.checked);
  label.classList.remove("content-day-active");
  if (input.checked) {
    label.classList.add("content-day-active");
  }
}
