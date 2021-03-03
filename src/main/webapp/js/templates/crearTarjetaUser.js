function tarjetaUser(usuario) {
  return `
    <!-- tarjeta Usuario -->
    <div class="card border-dark">
      <div class="bg-dark text-white card-header h3 font-weight-bold">
        ${usuario.usuario}
      </div>
      <div class="card-body text-dark text-center">
        <!-- datos Usuario -->
        <fieldset class="datos">
          <legend>Datos Personales</legend>
          <ul class="list-group list-group-flush">
            <li class="list-group-item">
              Nombre: ${usuario.apellidos} ${usuario.nombres}
            </li>
            <li class="list-group-item">Correo: ${usuario.email}</li>
            <li class="list-group-item">
              Edad: ${usuario?.edad ?? getEdad(usuario.fecha_nacimiento)}
            </li>
          </ul>
        </fieldset>
        <!-- actividades -->
        <fieldset class="actividades p-3 my-3">
          <legend>Actividades</legend>
        ${
          usuario.actividades
            ? usuario.actividades.reduce(
                (acc, actividad) => acc + tarjetaActividad(actividad),
                ""
              )
            : `<div class="actividad">
                    <h5 class="card-title">Sin Actividad</h5>
                </div>`
        }
        </fieldset>
        <a href="#" class="btn btn-dark agregarActividad" data-user='${JSON.stringify(
          usuario
        )}'>Agregar nueva actividad</a>
      </div>
    </div>
    `;
}

function tarjetaActividad(actividad) {
  return `
    <div class="actividad">
        <h5 class="card-title">${actividad.nombre}</h5>
        <p class="card-text">
            dias: ${actividad.dias} <br />
            hora: ${actividad.hora}hs
        </p>
    </div>
  `;
}
