//al iniciar la pagina
window.onload = async () => {
  await setInfoUser();
  if (!user.actividades || user.actividades.length == 0) {
    ActividadesHorarios(user);
  }
};
const DATOS_PAGO = {
  CBU: "0170272140000003562859",
  ALIAS: "FAIXA.PRETA.BJJ",
  EMAIL: "pruebas@cambiar.com",
  TITULAR: "OLIVERA FERNANDO JAVIER",
  CUIT: "20305026957",
};

//referencia HTML
const $btnFormRegistro = document.getElementById("btn-registro-familiar");
const $btnActividadesFamiliares = document.getElementById(
  "btn-actividades-familiares"
);
const $btnMenbresia = document.getElementById("data-user");
const $btnPago = document.getElementById("btn-pago");

//eventos
$btnFormRegistro.addEventListener("click", formRegistroFamiliar);
$btnActividadesFamiliares.addEventListener("click", actividadesFamiliares);
$btnMenbresia.addEventListener("click", mostrarMenbresia);
$btnPago.addEventListener("click", consultaPago);

function mostrarMenbresia() {
  clearInformacion();
  const div = document.createElement("DIV");
  div.classList.add("d-flex", "flex-equals");
  div.innerHTML = tarjetaUser(user);
  $informacion.appendChild(div);
  btnInscripciones();
}

async function ActividadesHorarios(usuario) {
  const data = await fetchData("actividadeshorarios");
  const actividades = data.filter(
    (a) =>
      !user.actividades.some(
        (au) => a.hora == au.hora && a["nombre actividad"] == au.nombre
      )
  );
  clearInformacion();
  const divTabla = document.createElement("DIV");
  const divDataUser = document.createElement("DIV");
  const divScroll = document.createElement("DIV");
  divScroll.classList.add("scroll");
  divTabla.appendChild(divScroll);
  $informacion.appendChild(divDataUser);
  $informacion.appendChild(divTabla);
  divDataUser.innerHTML = dataInscripcionUsuario(usuario);
  divScroll.innerHTML = createTableHTML(actividades, ["actividad deseada"]);
  crearBotonRegistro(divTabla, usuario);
  selectByRow();
}

function btnInscripciones() {
  const btns = Array.from(document.querySelectorAll(".agregarActividad"));
  btns.forEach((e) => {
    e.addEventListener("click", manejadorInscripcion);
  });
}

function manejadorInscripcion(event) {
  const element = event.target;
  const usuario = JSON.parse(element.dataset.user);
  ActividadesHorarios(usuario);
}

function dataInscripcionUsuario(usuario) {
  return `
  <div class="input-group w-content mx-auto my-4 flex-column">
    <div class="input-group-prepend">
      <span class="input-group-text">
        Inscribiendo a <div class="bg-info px-3 py-2 h5 text-white mx-2 my-0">${usuario.nombres} ${usuario.apellidos}</div>
      </span>
      <span class="input-group-text">Email: 
        <div class="bg-info px-3 py-2 h5 text-white mx-2 my-0">${usuario.email}</div>
      </span>
    </div>
  </div>
`;
}

function formRegistroFamiliar() {
  $informacion.innerHTML = crearFormularioRegistro(null, null, "default");
  const $btn = document.getElementById("sendForm");
  $btn.addEventListener("click", crearUsuario);
}

async function actividadesFamiliares() {
  clearInformacion();
  const familiares = await fetchData(`familiares?id_usuario=${user.id}`);
  console.log(familiares);
  const div = document.createElement("DIV");
  div.classList.add("d-flex", "flex-equals");
  familiares.forEach((f) => {
    div.innerHTML += tarjetaUser(f);
  });
  $informacion.appendChild(div);
  btnInscripciones();
}

async function consultaPago() {
  clearInformacion();
  const botones = document.createElement("DIV");
  const contenedor = document.createElement("DIV");
  const tablasActividades = await tablasPago(user, false);
  let contenidoBotonesHTML = "";
  //botones
  contenidoBotonesHTML += `
  <button id="marcar-actividades" class="btn btn-info d-none">
    Marcar Todas las Actividades
  </button>
  `;
  contenidoBotonesHTML += `<button class="btn btn-info" id="datos-pago"></button>`;

  botones.innerHTML = contenidoBotonesHTML;
  contenedor.appendChild(tablasActividades);
  contenedor.appendChild(botones);
  $informacion.appendChild(contenedor);
  selectByRow();
  //agregar eventos
  const btnMarcarActividades = document.getElementById("marcar-actividades");
  const btnDatosPago = document.getElementById("datos-pago");
  btnMarcarActividades.addEventListener("click", marcarActividades);
  btnDatosPago.addEventListener("click", datosPagar);

  actulizarBotonCheckout();
}

function datosPagar() {
  const objetoActividades = actividadesMarcadas();
  const actividades = objetoActividades.actividades.concat(
    objetoActividades.actividadesRegistro
  );
  if (user.pagoTotal) {
    cargarDatosModal();
  } else if (actividades.length == 0) {
    const alerta = {
      icon: "error",
      title: "Oops....",
      text: "Marque alguna actividad que desea pagar",
    };
    SwalAlert(alerta);
  } else {
    cargarDatosModal(actividades);
  }
  actulizarBotonCheckout();
}

async function registroUsuarioActividad(usuario) {
  const $inputaData = document.querySelectorAll(
    "input[name*=actividad]:checked"
  );
  let actividades = Array.from($inputaData).map((e) => JSON.parse(e.value));
  actividades = actividades.sort((a, b) =>
    a.nickname.localeCompare(b.nickname)
  );
  const formulario = new FormData();
  formulario.append("actividades", JSON.stringify(actividades));
  formulario.append("id_usuario", usuario.id);
  const config = {
    method: "POST",
    body: formulario,
    header: { "Content-Type": "application/x-www-form-urlencoded" },
  };
  const res = await fetchData("registroactividad", config);
  SwalAlert(res).then(() => setInfoUser());
  clearInformacion();
  // const strActividades = actividades.reduce(
  //   (acc, a) => acc + crearTarjetaActividad(a),
  //   ""
  // );
  // $informacion.innerHTML = tarjetaPago(strActividades);
  // cargarDatosModal(preference.items);
  // createPaymentButton(preference.id);
}

function tarjetaPago(actividades) {
  return `
  <div class="container">
    <div class="container">
    ${actividades}
    </div>
    <div>

    </div>
  </div>
  `;
}

function cargarDatosModal(actividades) {
  const contentModal = document.getElementById("modal-body");
  contentModal.innerHTML = "";
  const div = document.createElement("DIV");
  div.classList.add("d-flex", "flex-equals");
  if (actividades) {
    div.innerHTML += mensajePago(actividades);
  } else {
    div.innerHTML += ModalInfoPago();
  }
  contentModal.appendChild(div);

  $("#modal").modal();
  if (actividades) {
    //eventos
    const $btnPagoListo = document.getElementById("pago-listo");
    $btnPagoListo.addEventListener("click", () =>
      actividadesPagoListo(actividades)
    );
  }
}

function ModalInfoPago() {
  return `
  <ul class="list-group list-group-flush">
    <li class="list-group-item">CBU: ${DATOS_PAGO.CBU}</li>
    <li class="list-group-item">ALIAS: ${DATOS_PAGO.ALIAS}</li>
    <li class="list-group-item">Cuit: ${DATOS_PAGO.CUIT}</li>
    <li class="list-group-item">Titular: ${DATOS_PAGO.TITULAR}</li>
    <li class="list-group-item">Email: ${DATOS_PAGO.EMAIL}</li>
    <li class="list-group-item">Total a Pagar: ${user.pagoTotal}</li>
  </ul>
  `;
}

function mensajePago(actividades) {
  let textoActividades = actividades.reduce(
    (acc, e) => acc + `${e.title}  `,
    ""
  );
  const pagoTotal = actividades.reduce((acc, e) => acc + e.unitPrice, 0);
  user.pagoTotal = pagoTotal;
  return `
  <div>
    <p>
      Las actividades que estoy pagando son las siguientes: <br>
      ${textoActividades}
      <br>
      El total a pagar es: ${pagoTotal}
    </p>
    <label>
      <input type="checkbox" id="input-pago-listo">
      Estoy de acuerdo en pagar las actividades mencionadas
    </label>
    <button class="btn btn-success" id="pago-listo">Enviar</button>
  </div>
  `;
}

async function actividadesPagoListo(actividades) {
  const esPagoListo = document.getElementById("input-pago-listo").checked;
  if (esPagoListo) {
    console.log("registro pago listo en las actividades");
    const formulario = new FormData();
    formulario.append("actividades", JSON.stringify(actividades));
    const config = {
      method: "PUT",
      body: formulario,
      header: { "Content-Type": "application/x-www-form-urlencoded" },
    };
    const data = await fetchData("pagolisto", config);
    SwalAlert(data);
    cargarDatosModal();
    $("#modal").on("hide.bs.modal", consultaPago);
  } else if (!esPagoListo) {
    const alerta = {
      icon: "error",
      title: "Oops....",
      text: "Marque la casilla",
    };
    SwalAlert(alerta);
  }
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

function crearBotonRegistro(divTabla, usuario) {
  divTabla.innerHTML += `
  <button class="btn btn-info" id="btn-anotarse">
    Anotarse a la Actividad
  </button>
  `;
  const $btnAnotarse = document.getElementById("btn-anotarse");
  $btnAnotarse.addEventListener("click", () =>
    registroUsuarioActividad(usuario)
  );
}

function actulizarBotonCheckout() {
  const btn = document.getElementById("datos-pago");
  const actividadesPago = document.querySelector("input[name=pago]");
  const actividadesPagoRegistro = document.querySelector(
    "input[name=pago-registro]"
  );
  const actividadesPagoProceso = document.getElementById("Procesando Pago");
  btn.classList.remove("d-none");
  if (actividadesPago != null || actividadesPagoRegistro != null) {
    btn.textContent = "Actividades a Pagar";
  } else if (actividadesPagoProceso != null) {
    btn.textContent = "Datos para Pagar";
  } else {
    btn.classList.add("d-none");
  }
}

function createPaymentButton(preferenceId) {
  const script = document.createElement("script");
  script.src =
    "https://www.mercadopago.com.ar/integrations/v1/web-payment-checkout.js";
  script.type = "text/javascript";
  script.dataset.preferenceId = preferenceId;
  document.getElementById("div-checkout").innerHTML = "";
  document.querySelector("#div-checkout").appendChild(script);
}
