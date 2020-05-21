package ec.telconet.elemento.controller;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import ec.telconet.elemento.service.MarcaElementoService;
import ec.telconet.microservicio.dependencia.util.dto.PageDTO;
import ec.telconet.microservicio.dependencia.util.response.GenericBasicResponse;
import ec.telconet.microservicio.dependencia.util.response.GenericListResponse;
import ec.telconet.microservicios.dependencias.esquema.infraestructura.entity.AdmiMarcaElemento;

/**
 * Clase utilizada para publicar microservicios técnicos con información referente a la marca de los elementos
 *
 * @author Marlon Plúas <mailto:mpluas@telconet.ec>
 * @version 1.0
 * @since 02/03/2020
 */
@RestController
@RequestMapping
public class MarcaElementoController {
	Logger log = LogManager.getLogger(this.getClass());
	
	@Autowired
	MarcaElementoService marcaElementoService;
	
	/**
	 * Método que retorna la lista de marcas de elementos
	 * 
	 * @author Marlon Plúas <mailto:mpluas@telconet.ec>
	 * @version 1.0
	 * @since 02/03/2020
	 * 
	 * @return {@linkplain GenericListResponse}
	 * @throws Exception
	 */
	@GetMapping("listaMarcaElemento")
	public GenericListResponse<AdmiMarcaElemento> listaMarcaElemento() throws Exception {
		log.info("Petición recibida: listaMarcaElemento");
		GenericListResponse<AdmiMarcaElemento> response = new GenericListResponse<AdmiMarcaElemento>();
		response.setData(marcaElementoService.listaMarcaElemento());
		return response;
	}
	
	/**
	 * Método que retorna la lista de marcas de elementos con filtros
	 * 
	 * @author Marlon Plúas <mailto:mpluas@telconet.ec>
	 * @version 1.0
	 * @since 02/03/2020
	 * 
	 * @param request {@linkplain AdmiMarcaElemento}
	 * @return {@linkplain GenericListResponse}
	 * @throws Exception
	 */
	@PostMapping(path = "listaMarcaElementoPor", consumes = "application/json")
	public GenericListResponse<AdmiMarcaElemento> listaMarcaElementoPor(@RequestBody AdmiMarcaElemento request) throws Exception {
		log.info("Petición recibida: listaMarcaElementoPor");
		GenericListResponse<AdmiMarcaElemento> response = new GenericListResponse<AdmiMarcaElemento>();
		response.setData(marcaElementoService.listaMarcaElementoPor(request));
		return response;
	}
	
	/**
	 * Método que retorna la paginación de una lista de marcas de elementos con filtros
	 * 
	 * @author Marlon Plúas <mailto:mpluas@telconet.ec>
	 * @version 1.0
	 * @since 02/03/2020
	 * 
	 * @param request {@linkplain PageDTO}
	 * @return {@linkplain GenericBasicResponse}
	 * @throws Exception
	 */
	@PostMapping(path = "paginaListaMarcaElementoPor", consumes = "application/json")
	public GenericBasicResponse<Page<AdmiMarcaElemento>> paginaListaMarcaElementoPor(@RequestBody PageDTO<AdmiMarcaElemento> request)
			throws Exception {
		log.info("Petición recibida: paginaListaMarcaElementoPor");
		GenericBasicResponse<Page<AdmiMarcaElemento>> response = new GenericBasicResponse<Page<AdmiMarcaElemento>>();
		response.setData(marcaElementoService.paginaListaMarcaElementoPor(request));
		return response;
	}
}
