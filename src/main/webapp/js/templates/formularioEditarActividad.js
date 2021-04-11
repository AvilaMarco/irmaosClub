function formEditarActividad(actividad, instructoresdata) {
  const nombre = actividad.nombre;
  const idInstructor = actividad.id_menbresia;
  const nombreInstructor = actividad.instructor;
  const idHorario = actividad.id_horario;
  const hora = actividad.hora;
  const idActividad = actividad.id_actividad;
  const precio = actividad.precio;
  const arrDias = actividad.dias.split(", ");
  const dias = diasSemana(arrDias);
  const instructores = optionsInstructores(instructoresdata, idInstructor);
  return `
    <div>
        <h2>Actividad</h2>
        <input type="text" id="${idActividad}" name="actividad" class="form-control" value="${nombre}">
        <h2>Instructor</h2>
        <select name="instructor" class="form-control">
            <option value="${idInstructor}" selected>${nombreInstructor}</option>
            ${instructores}
        </select>
        <h2>Precio</h2>
        <input type="number" name="precio" value="${precio}" class="form-control" min="0" step="100">        
        <h2>Horarios</h2>
        <input type="number" id="${idHorario}" name="horario" value="${hora}" class="form-control" min="8" max="24">
        <h2>Dias</h2>
        <div class="content-semana">
            ${dias}
        </div>
    </div>
    `;
}

function obtenerDatos() {
  const actividadModificada = {};
  const actividad = document.querySelector("[name=actividad]");
  const idInstructor = document.querySelector("[name=instructor]").value;
  const precio = document.querySelector("[name=precio]").value;
  const horario = document.querySelector("[name=horario]");
  const inputDias = Array.from(
    document.querySelectorAll("[name=dias]:checked")
  );
  const dias = inputDias.map((e) => e.value);
  actividadModificada.idActividad = actividad.id;
  actividadModificada.actividad = actividad.value;
  actividadModificada.idInstructor = idInstructor;
  actividadModificada.idHorario = horario.id;
  actividadModificada.hora = horario.value;
  actividadModificada.dias = dias;
  actividadModificada.precio = precio;
  return actividadModificada;
}

function optionsInstructores(dataInstructors, idInstructor) {
  const instructores = dataInstructors.filter(
    (e) => e.id_menbresia !== idInstructor
  );
  return instructores.reduce(
    (options, e) =>
      options + `<option value="${e.id_menbresia}">${e.nombre}</option>`,
    ""
  );
}
