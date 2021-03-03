function crearFormularioRegistro(crear, editar, registro) {
  return `
    <form class="edit-form" id="formulario-general">
  
      <div class="form-row">
        <div class="form-group col">
          <label> Usuario </label>
          <input 
            type="text" 
            class="form-control" 
            name="usuario" 
            placeholder="Usuario" 
            value="${editar?.usuario ?? ""}"
            required
          >
        </div>
        <div class="form-group col">
          <label> Nombres </label>
          <input 
            type="text" 
            class="form-control" 
            name="nombres" 
            placeholder="Nombres" 
            value="${editar?.nombres ?? ""}"
            required
          >
        </div>
        <div class="form-group col">
          <label> Apellidos </label>
          <input 
            type="text" 
            class="form-control" 
            name="apellidos" 
            placeholder="Apellidos" 
            value="${editar?.apellidos ?? ""}"
            required
          >
        </div>
      </div>
  
      <div class="form-row">
        <div class="form-group col">
          <label> Correo </label>
          <input 
            type="email" 
            class="form-control" 
            placeholder="Correo" 
            name="email"
            value="${editar?.email ?? ""}"
            required
          >
        </div>
        ${
          crear || registro
            ? `<div class="form-group col">
                <label> Password </label>
                <input 
                  type="password" 
                  class="form-control" 
                  name="password"
                  required
                >
              </div>`
            : ""
        }
      </div>
  
      <div class="form-row">
        <div class="form-group col">
          <label> Celular </label>
          <input 
            type="number" 
            min="0" 
            class="form-control" 
            placeholder="Celular" 
            name="celular"
            value="${editar?.celular ?? ""}"
            required
          >
        </div>
        <div class="form-group col">
          <label> Celular Emergencia </label>
          <input 
            type="number" 
            min="0" 
            class="form-control" 
            placeholder="Celular Emergencia"
            name="celular_emergencia" 
            value="${editar?.celular_emergencia ?? ""}"
            required
          >
        </div>
      </div>
  
      <div class="form-row">
        ${
          crear || registro
            ? `<div class="form-group col">
                <label> Fecha de Nacimiento </label>
                <input 
                  type="date" 
                  class="form-control" 
                  placeholder="Fecha de Nacimiento" 
                  name="fecha_nacimiento"
                  required
                >
              </div>`
            : ""
        }
        ${
          crear || registro
            ? `<div class="form-group col">
                <label> DNI </label>
                <input 
                  type="number" 
                  min="0" 
                  max="99999999" 
                  class="form-control" 
                  placeholder="DNI" 
                  name="dni"
                  required
                >
              </div>`
            : ""
        }
        ${
          crear
            ? `<div class="form-group col">
              <label> Id Titular </label>
              <input type="number" min="0" class="form-control" placeholder="Id Titular">
            </div>`
            : ""
        }
        ${
          crear
            ? `<div class="form-group col">
              <label> Rol </label>
              <select class="form-control" name="id_rol">
                <option selected value="1">Cliente</option>
                <option value="2">Instructor</option>
              </select>
            </div>`
            : ""
        }
      </div>
  
      <div class="form-row">
        <div class="form-group col">
          <label> Direccion </label>
          <input 
            type="text" 
            class="form-control" 
            placeholder="calle numero de casa" 
            name="direccion"
            value="${editar?.direccion ?? ""}"
            required
          >
        </div>
        ${
          crear || editar
            ? `<div class="form-group col">
              <label>
                Certificado Salud
              </label>
              <select class="form-control" name="certificado_salud" >
                <option value="0">NO</option>
                <option value="1" ${
                  editar?.certificado_salud ? "selected" : ""
                }>SI</option>
              </select>
            </div>`
            : ""
        }
        ${
          crear || editar
            ? `<div class="form-group col">
              <label> Activo </label>
              <select class="form-control" name="activo">
                <option value="0">NO</option>
                <option value="1" ${
                  editar?.activo ? "selected" : ""
                }>SI</option>
              </select>
            </div>`
            : ""
        }
      </div>
      <div class="form-row">
        <div class="form-group col">
          <label> Observacion  </label>
          <textarea 
            class="form-control" 
            placeholder="a tener en cuenta..." 
            name="observacion"
          >${editar?.observacion ?? ""}</textarea>
        </div>
      </div>
      ${
        crear || registro
          ? `<button class="btn btn-outline-secondary" id="sendForm">Enviar</button>`
          : ""
      } 
    </form>
    `;
}
