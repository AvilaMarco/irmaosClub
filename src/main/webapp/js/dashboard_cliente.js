//al iniciar la pagina
window.onload = async () => {
  await setInfoUser();
  if (!user.actividad) {
    ActividadesHorarios();
  }
};
//referencia HTML
const btnFormRegistro = document.getElementById("btn-registro-familiar");

//eventos
btnFormRegistro.addEventListener("click", formRegistroFamiliar);

async function ActividadesHorarios() {
  const data = await fetchData("actividadeshorarios");
  clearInformacion();
  $informacion.innerHTML = createTableHTML(data, ["actividad deseada"]);
  $informacion.innerHTML += `
  <button class="btn btn-info" id="btn-anotarse">
    Anotarse a la Actividad
  </button>
  `;
  const $btnAnotarse = document.getElementById("btn-anotarse");
  $btnAnotarse.addEventListener("click", registroUsuarioActividad);
}

function formRegistroFamiliar() {
  $informacion.innerHTML = crearFormularioRegistro(null, null, "default");
}

function registroFamiliar() {}

async function registroUsuarioActividad() {
  const $inputaData = document.querySelectorAll(
    "input[name*=actividad]:checked"
  );
  let actividades = Array.from($inputaData).map((e) => JSON.parse(e.value));
  actividades = actividades.sort((a, b) =>
    a.nickname.localeCompare(b.nickname)
  );
  const formulario = new FormData();
  formulario.append("actividades", JSON.stringify(actividades));
  formulario.append("id_usuario", user.id);
  const config = {
    method: "POST",
    body: formulario,
    header: { "Content-Type": "application/x-www-form-urlencoded" },
  };
  const preference = await fetchData("checkout", config);
  console.log(preference);
  clearInformacion();
  const strActividades = actividades.reduce(
    (acc, a) => acc + crearTarjetaActividad(a),
    ""
  );
  $informacion.innerHTML = tarjetaPago(strActividades);
  createCheckoutButton(preference.id);
}

function tarjetaPago(actividades) {
  return `
  <div class="container">
    <div class="container">
    ${actividades}
    </div>
    <div id="div-checkout">
    </div>
  </div>
  `;
}

function crearTarjetaActividad(actividad) {
  return `
  <div class="card bg-dark text-white">
    <div class="card-body text-center">
      <h4 class="card-title">${actividad["nombre actividad"]}</h4>
      <p class="card-text">${actividad.hora} - ${actividad.dias}</p>
      <button class="btn btn-success m-2">$${actividad.precio}</button>
    </div>
  </div>
  `;
}

//Create preference when click on checkout button
function createCheckoutButton(preferenceId) {
  const script = document.createElement("script");

  // The source domain must be completed according to the site for which you are integrating.
  // For example: for Argentina ".com.ar" or for Brazil ".com.br".
  script.src =
    "https://www.mercadopago.com.ar/integrations/v1/web-payment-checkout.js";
  script.type = "text/javascript";
  script.dataset.preferenceId = preferenceId;
  document.getElementById("div-checkout").innerHTML = "";
  document.querySelector("#div-checkout").appendChild(script);
}
