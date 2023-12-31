package ec.telconet.elemento.kafka;

import java.util.*;
import java.util.concurrent.TimeUnit;

import ec.telconet.microservicio.core.tecnico.kafka.request.*;
import ec.telconet.microservicio.dependencia.util.kafka.KafkaProperties;
import ec.telconet.microservicios.dependencias.esquema.infraestructura.dto.*;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.support.Acknowledgment;
import org.springframework.messaging.handler.annotation.Header;
import org.springframework.messaging.handler.annotation.SendTo;
import org.springframework.stereotype.Component;

import ec.telconet.elemento.service.ConsultasService;
import ec.telconet.elemento.service.DetalleElementoService;
import ec.telconet.elemento.service.ElementoService;
import ec.telconet.elemento.service.HistorialElementoService;
import ec.telconet.elemento.service.MarcaElementoService;
import ec.telconet.elemento.service.ModeloElementoService;
import ec.telconet.elemento.service.TipoElementoService;
import ec.telconet.elemento.service.TransaccionesService;
import ec.telconet.microservicio.core.tecnico.kafka.cons.CoreTecnicoConstants;
import ec.telconet.microservicio.dependencia.util.exception.GenericException;
import ec.telconet.microservicio.dependencia.util.general.Formato;
import ec.telconet.microservicio.dependencia.util.kafka.KafkaRequest;
import ec.telconet.microservicio.dependencia.util.kafka.KafkaResponse;
import ec.telconet.microservicios.dependencias.esquema.infraestructura.entity.AdmiMarcaElemento;
import ec.telconet.microservicios.dependencias.esquema.infraestructura.entity.AdmiModeloElemento;
import ec.telconet.microservicios.dependencias.esquema.infraestructura.entity.AdmiTipoElemento;
import ec.telconet.microservicios.dependencias.esquema.infraestructura.entity.InfoDetalleElemento;
import ec.telconet.microservicios.dependencias.esquema.infraestructura.entity.InfoElemento;
import ec.telconet.microservicios.dependencias.esquema.infraestructura.entity.InfoHistorialElemento;

/**
 * Clase utilizada para consumir OP sincrónico o asincrónico en kafka
 *
 * @author Marlon Plúas <mailto:mpluas@telconet.ec>
 * @version 1.0
 * @since 02/03/2020
 */
@Component
public class ServiceConsumer {
    Logger log = LogManager.getLogger(this.getClass());

    @Value("${kafka.request-reply.timeout-ms:300s}")
    private String replyTimeout;

    @Autowired
    ElementoService elementoService;

    @Autowired
    DetalleElementoService detalleElementoService;

    @Autowired
    HistorialElementoService historialElementoService;

    @Autowired
    MarcaElementoService marcaElementoService;

    @Autowired
    ModeloElementoService modeloElementoService;

    @Autowired
    TipoElementoService tipoElementoService;

    @Autowired
    ConsultasService consultasService;

    @Autowired
    TransaccionesService transaccionesService;

    @Autowired
    KafkaProperties kafkaProperties;

    @Autowired
    public ServiceConsumer(KafkaProperties kafkaProperties) {
        kafkaProperties.setTopicGroup(CoreTecnicoConstants.GROUP_ELEMENTO);
        log.info("Grupo kafka configurado: {}", kafkaProperties.getTopicGroup());
        Collection<String> colKafkaTopicSync = Collections.singletonList(CoreTecnicoConstants.TOPIC_ELEMENTO_SYNC);
        Collection<String> topicsSync = new ArrayList<>();
        if (kafkaProperties.getTopicSyncSufijo() != null) {
            colKafkaTopicSync.stream().distinct().forEach(v -> topicsSync.add(v.concat(kafkaProperties.getTopicSyncSufijo())));
            kafkaProperties.setTopicSync(topicsSync);
        } else {
            kafkaProperties.setTopicSync(colKafkaTopicSync);
        }
        log.info("Topic kafka sync configurado: {}", kafkaProperties.getTopicSync()::toString);

        Collection<String> colKafkaTopicAync = Collections.singletonList(CoreTecnicoConstants.TOPIC_ELEMENTO_ASYN);
        Collection<String> topicsAsyn = new ArrayList<>();
        if (kafkaProperties.getTopicAsynSufijo() != null) {
            colKafkaTopicAync.stream().distinct().forEach(v -> topicsAsyn.add(v.concat(kafkaProperties.getTopicAsynSufijo())));
            kafkaProperties.setTopicAsyn(topicsAsyn);
        } else {
            kafkaProperties.setTopicAsyn(colKafkaTopicAync);
        }
        log.info("Topic kafka asyn configurado: {}", kafkaProperties.getTopicAsyn()::toString);
        this.kafkaProperties = kafkaProperties;
    }

    /**
     * Listener asincrónico kafka
     *
     * @param kafkaRequest Request Kafka
     *
     * @author Marlon Plúas <mailto:mpluas@telconet.ec>
     * @since 02/03/2020
     */
    @KafkaListener(topics = "#{kafkaProperties.getTopicAsyn()}", groupId = "#{kafkaProperties.getTopicGroup()}", containerFactory = "kafkaListenerContainerFactory")
    public void serviceAsynchroListener(KafkaRequest<?> kafkaRequest) {
        String idTransKafka = UUID.randomUUID().toString();
        log.info("Petición kafka asincrónico recibida: {}, Transacción: {}", kafkaRequest.getOp(), idTransKafka);
        // EJECUCIONES ASINCRONICAS
        try {
            log.info("Petición kafka asincrónico enviada: {}, Transacción: {}", kafkaRequest.getOp(), idTransKafka);
            throw new GenericException("No existe OP configurados, borre esta linea cuando se genere uno");
        } catch (Exception e) {
            log.error(e);
            log.info("Petición kafka asincrónico enviada: {}, Transacción: {}, Estado: Fallida", kafkaRequest.getOp(), idTransKafka);
        }
    }

    /**
     * Listener sincrónico kafka
     *
     * @param <T>          Objeto de respuesta
     * @param kafkaRequest Request Kafka
     *
     * @return KafkaResponse
     *
     * @author Marlon Plúas <mailto:mpluas@telconet.ec>
     * @since 02/03/2020
     */
    @SuppressWarnings("unchecked")
    @KafkaListener(topics = "#{kafkaProperties.getTopicSync()}", groupId = "#{kafkaProperties.getTopicGroup()}", containerFactory = "requestReplyListenerContainerFactory")
    @SendTo()
    public <T> KafkaResponse<T> serviceSynchroListener(KafkaRequest<?> kafkaRequest, @Header(value = "dateSendMessage", defaultValue = "") String dateSendMessage,
                                                       Acknowledgment commitKafka) {
        String idTransKafka = UUID.randomUUID().toString();
        log.info("Petición kafka sincrónico recibida: {}, Transacción: {}", kafkaRequest.getOp(), idTransKafka);
        KafkaResponse<String> kafkaResponse = new KafkaResponse<>();
        try {
            validateConsumerMessage(dateSendMessage);
            if (kafkaRequest.getOp().equalsIgnoreCase(CoreTecnicoConstants.OP_GUARDAR_DETALLE_ELEMENTO)) {
                DetalleElementoKafkaReq data = Formato.mapearObjDeserializado(kafkaRequest.getData(), DetalleElementoKafkaReq.class);
                // Inicio Proceso logico
                InfoDetalleElemento requestService = Formato.mapearObjDeserializado(data, InfoDetalleElemento.class);
                // Fin Proceso logico
                KafkaResponse<InfoDetalleElemento> response = new KafkaResponse<>();
                response.setData(Collections.singletonList(detalleElementoService.guardarDetalleElemento(requestService)));
                commitKafka.acknowledge();
                log.info("Petición kafka sincrónico enviada: {}, Transacción: {}", kafkaRequest.getOp(), idTransKafka);
                return (KafkaResponse<T>) response;
            } else if (kafkaRequest.getOp().equalsIgnoreCase(CoreTecnicoConstants.OP_ACTUALIZAR_DETALLE_ELEMENTO)) {
                DetalleElementoKafkaReq data = Formato.mapearObjDeserializado(kafkaRequest.getData(), DetalleElementoKafkaReq.class);
                // Inicio Proceso logico
                InfoDetalleElemento requestService = Formato.mapearObjDeserializado(data, InfoDetalleElemento.class);
                // Fin Proceso logico
                KafkaResponse<InfoDetalleElemento> response = new KafkaResponse<>();
                response.setData(Collections.singletonList(detalleElementoService.actualizarDetalleElemento(requestService)));
                commitKafka.acknowledge();
                log.info("Petición kafka sincrónico enviada: {}, Transacción: {}", kafkaRequest.getOp(), idTransKafka);
                return (KafkaResponse<T>) response;
            } else if (kafkaRequest.getOp().equalsIgnoreCase(CoreTecnicoConstants.OP_ELIMINAR_DETALLE_ELEMENTO)) {
                DetalleElementoKafkaReq data = Formato.mapearObjDeserializado(kafkaRequest.getData(), DetalleElementoKafkaReq.class);
                // Inicio Proceso logico
                InfoDetalleElemento requestService = Formato.mapearObjDeserializado(data, InfoDetalleElemento.class);
                // Fin Proceso logico
                KafkaResponse<Boolean> response = new KafkaResponse<>();
                response.setData(Collections.singletonList(detalleElementoService.eliminarDetalleElemento(requestService)));
                commitKafka.acknowledge();
                log.info("Petición kafka sincrónico enviada: {}, Transacción: {}", kafkaRequest.getOp(), idTransKafka);
                return (KafkaResponse<T>) response;
            } else if (kafkaRequest.getOp().equalsIgnoreCase(CoreTecnicoConstants.OP_LISTA_DETALLE_ELEMENTO)) {
                KafkaResponse<InfoDetalleElemento> response = new KafkaResponse<>();
                response.setData(detalleElementoService.listaDetalleElemento());
                commitKafka.acknowledge();
                log.info("Petición kafka sincrónico enviada: {}, Transacción: {}", kafkaRequest.getOp(), idTransKafka);
                return (KafkaResponse<T>) response;
            } else if (kafkaRequest.getOp().equalsIgnoreCase(CoreTecnicoConstants.OP_LISTA_DETALLE_ELEMENTO_POR)) {
                DetalleElementoKafkaReq data = Formato.mapearObjDeserializado(kafkaRequest.getData(), DetalleElementoKafkaReq.class);
                // Inicio Proceso logico
                InfoDetalleElemento requestService = Formato.mapearObjDeserializado(data, InfoDetalleElemento.class);
                // Fin Proceso logico
                KafkaResponse<InfoDetalleElemento> response = new KafkaResponse<>();
                response.setData(detalleElementoService.listaDetalleElementoPor(requestService));
                commitKafka.acknowledge();
                log.info("Petición kafka sincrónico enviada: {}, Transacción: {}", kafkaRequest.getOp(), idTransKafka);
                return (KafkaResponse<T>) response;
            } else if (kafkaRequest.getOp().equalsIgnoreCase(CoreTecnicoConstants.OP_LISTA_DETALLE_ELEMENTO_POR_ELEMENTO)) {
                DetalleElementoKafkaReq data = Formato.mapearObjDeserializado(kafkaRequest.getData(), DetalleElementoKafkaReq.class);
                // Inicio Proceso logico
                DetalleElementoReqDTO requestService = Formato.mapearObjDeserializado(data, DetalleElementoReqDTO.class);
                // Fin Proceso logico
                KafkaResponse<InfoDetalleElemento> response = new KafkaResponse<>();
                response.setData(detalleElementoService.listaDetalleElementoPorElemento(requestService));
                commitKafka.acknowledge();
                log.info("Petición kafka sincrónico enviada: {}, Transacción: {}", kafkaRequest.getOp(), idTransKafka);
                return (KafkaResponse<T>) response;
            } else if (kafkaRequest.getOp().equalsIgnoreCase(CoreTecnicoConstants.OP_GUARDAR_ELEMENTO)) {
                ElementoKafkaReq data = Formato.mapearObjDeserializado(kafkaRequest.getData(), ElementoKafkaReq.class);
                // Inicio Proceso logico
                InfoElemento requestService = Formato.mapearObjDeserializado(data, InfoElemento.class);
                // Fin Proceso logico
                KafkaResponse<InfoElemento> response = new KafkaResponse<>();
                response.setData(Collections.singletonList(elementoService.guardarElemento(requestService)));
                commitKafka.acknowledge();
                log.info("Petición kafka sincrónico enviada: {}, Transacción: {}", kafkaRequest.getOp(), idTransKafka);
                return (KafkaResponse<T>) response;
            } else if (kafkaRequest.getOp().equalsIgnoreCase(CoreTecnicoConstants.OP_ACTUALIZAR_ELEMENTO)) {
                ElementoKafkaReq data = Formato.mapearObjDeserializado(kafkaRequest.getData(), ElementoKafkaReq.class);
                // Inicio Proceso logico
                InfoElemento requestService = Formato.mapearObjDeserializado(data, InfoElemento.class);
                // Fin Proceso logico
                KafkaResponse<InfoElemento> response = new KafkaResponse<>();
                response.setData(Collections.singletonList(elementoService.actualizarElemento(requestService)));
                commitKafka.acknowledge();
                log.info("Petición kafka sincrónico enviada: {}, Transacción: {}", kafkaRequest.getOp(), idTransKafka);
                return (KafkaResponse<T>) response;
            } else if (kafkaRequest.getOp().equalsIgnoreCase(CoreTecnicoConstants.OP_ELIMINAR_ELEMENTO)) {
                ElementoKafkaReq data = Formato.mapearObjDeserializado(kafkaRequest.getData(), ElementoKafkaReq.class);
                // Inicio Proceso logico
                InfoElemento requestService = Formato.mapearObjDeserializado(data, InfoElemento.class);
                // Fin Proceso logico
                KafkaResponse<Boolean> response = new KafkaResponse<>();
                response.setData(Collections.singletonList(elementoService.eliminarElemento(requestService)));
                commitKafka.acknowledge();
                log.info("Petición kafka sincrónico enviada: {}, Transacción: {}", kafkaRequest.getOp(), idTransKafka);
                return (KafkaResponse<T>) response;
            } else if (kafkaRequest.getOp().equalsIgnoreCase(CoreTecnicoConstants.OP_LISTA_ELEMENTO)) {
                KafkaResponse<InfoElemento> response = new KafkaResponse<>();
                response.setData(elementoService.listaElemento());
                commitKafka.acknowledge();
                log.info("Petición kafka sincrónico enviada: {}, Transacción: {}", kafkaRequest.getOp(), idTransKafka);
                return (KafkaResponse<T>) response;
            } else if (kafkaRequest.getOp().equalsIgnoreCase(CoreTecnicoConstants.OP_LISTA_ELEMENTO_POR)) {
                ElementoKafkaReq data = Formato.mapearObjDeserializado(kafkaRequest.getData(), ElementoKafkaReq.class);
                // Inicio Proceso logico
                InfoElemento requestService = Formato.mapearObjDeserializado(data, InfoElemento.class);
                // Fin Proceso logico
                KafkaResponse<InfoElemento> response = new KafkaResponse<>();
                response.setData(elementoService.listaElementoPor(requestService));
                commitKafka.acknowledge();
                log.info("Petición kafka sincrónico enviada: {}, Transacción: {}", kafkaRequest.getOp(), idTransKafka);
                return (KafkaResponse<T>) response;
            } else if (kafkaRequest.getOp().equalsIgnoreCase(CoreTecnicoConstants.OP_LISTA_ELEMENTO_POR_TIPO)) {
                ElementoKafkaReq data = Formato.mapearObjDeserializado(kafkaRequest.getData(), ElementoKafkaReq.class);
                // Inicio Proceso logico
                ElementoPorTipoReqDTO requestService = Formato.mapearObjDeserializado(data, ElementoPorTipoReqDTO.class);
                // Fin Proceso logico
                KafkaResponse<InfoElemento> response = new KafkaResponse<>();
                response.setData(elementoService.listaElementoPorTipo(requestService));
                commitKafka.acknowledge();
                log.info("Petición kafka sincrónico enviada: {}, Transacción: {}", kafkaRequest.getOp(), idTransKafka);
                return (KafkaResponse<T>) response;
            } else if (kafkaRequest.getOp().equalsIgnoreCase(CoreTecnicoConstants.OP_LISTA_ELEMENTO_POR_ES_MONITORIZADO)) {
                ElementoKafkaReq data = Formato.mapearObjDeserializado(kafkaRequest.getData(), ElementoKafkaReq.class);
                // Inicio Proceso logico
                ElementoPorMonitorizadoReqDTO requestService = Formato.mapearObjDeserializado(data, ElementoPorMonitorizadoReqDTO.class);
                // Fin Proceso logico
                KafkaResponse<InfoElemento> response = new KafkaResponse<>();
                response.setData(elementoService.listaElementoPorEsMonitorizado(requestService));
                commitKafka.acknowledge();
                log.info("Petición kafka sincrónico enviada: {}, Transacción: {}", kafkaRequest.getOp(), idTransKafka);
                return (KafkaResponse<T>) response;
            } else if (kafkaRequest.getOp().equalsIgnoreCase(CoreTecnicoConstants.OP_LISTA_ELEMENTO_POR_REGION_PARAMS)) {
                ElementoKafkaReq data = Formato.mapearObjDeserializado(kafkaRequest.getData(), ElementoKafkaReq.class);
                // Inicio Proceso logico
                ElementoPorRegionParamsReqDTO requestService = Formato.mapearObjDeserializado(data, ElementoPorRegionParamsReqDTO.class);
                // Fin Proceso logico
                KafkaResponse<InfoElemento> response = new KafkaResponse<>();
                response.setData(elementoService.listaElementoPorRegionParams(requestService));
                commitKafka.acknowledge();
                log.info("Petición kafka sincrónico enviada: {}, Transacción: {}", kafkaRequest.getOp(), idTransKafka);
                return (KafkaResponse<T>) response;
            } else if (kafkaRequest.getOp().equalsIgnoreCase(CoreTecnicoConstants.OP_LISTA_ELEMENTO_POR_PROVINCIA_PARAMS)) {
                ElementoKafkaReq data = Formato.mapearObjDeserializado(kafkaRequest.getData(), ElementoKafkaReq.class);
                // Inicio Proceso logico
                ElementoPorProvinciaParamsReqDTO requestService = Formato.mapearObjDeserializado(data, ElementoPorProvinciaParamsReqDTO.class);
                // Fin Proceso logico
                KafkaResponse<InfoElemento> response = new KafkaResponse<>();
                response.setData(elementoService.listaElementoPorProvinciaParams(requestService));
                commitKafka.acknowledge();
                log.info("Petición kafka sincrónico enviada: {}, Transacción: {}", kafkaRequest.getOp(), idTransKafka);
                return (KafkaResponse<T>) response;
            } else if (kafkaRequest.getOp().equalsIgnoreCase(CoreTecnicoConstants.OP_LISTA_ELEMENTO_POR_PARROQUIA_PARAMS)) {
                ElementoKafkaReq data = Formato.mapearObjDeserializado(kafkaRequest.getData(), ElementoKafkaReq.class);
                // Inicio Proceso logico
                ElementoPorParroquiaParamsReqDTO requestService = Formato.mapearObjDeserializado(data, ElementoPorParroquiaParamsReqDTO.class);
                // Fin Proceso logico
                KafkaResponse<InfoElemento> response = new KafkaResponse<>();
                response.setData(elementoService.listaElementoPorParroquiaParams(requestService));
                commitKafka.acknowledge();
                log.info("Petición kafka sincrónico enviada: {}, Transacción: {}", kafkaRequest.getOp(), idTransKafka);
                return (KafkaResponse<T>) response;
            } else if (kafkaRequest.getOp().equalsIgnoreCase(CoreTecnicoConstants.OP_LISTA_ELEMENTO_POR_CANTON_PARAMS)) {
                ElementoKafkaReq data = Formato.mapearObjDeserializado(kafkaRequest.getData(), ElementoKafkaReq.class);
                // Inicio Proceso logico
                ElementoPorCantonParamsReqDTO requestService = Formato.mapearObjDeserializado(data, ElementoPorCantonParamsReqDTO.class);
                // Fin Proceso logico
                KafkaResponse<InfoElemento> response = new KafkaResponse<>();
                response.setData(elementoService.listaElementoPorCantonParams(requestService));
                commitKafka.acknowledge();
                log.info("Petición kafka sincrónico enviada: {}, Transacción: {}", kafkaRequest.getOp(), idTransKafka);
                return (KafkaResponse<T>) response;
            } else if (kafkaRequest.getOp().equalsIgnoreCase(CoreTecnicoConstants.OP_LISTA_ELEMENTO_POR_FILIAL_PARAMS)) {
                ElementoKafkaReq data = Formato.mapearObjDeserializado(kafkaRequest.getData(), ElementoKafkaReq.class);
                // Inicio Proceso logico
                ElementoPorFilialParamsReqDTO requestService = Formato.mapearObjDeserializado(data, ElementoPorFilialParamsReqDTO.class);
                // Fin Proceso logico
                KafkaResponse<InfoElemento> response = new KafkaResponse<>();
                response.setData(elementoService.listaElementoPorFilialParams(requestService));
                commitKafka.acknowledge();
                log.info("Petición kafka sincrónico enviada: {}, Transacción: {}", kafkaRequest.getOp(), idTransKafka);
                return (KafkaResponse<T>) response;
            } else if (kafkaRequest.getOp().equalsIgnoreCase(CoreTecnicoConstants.OP_GUARDAR_HISTORIAL_ELEMENTO)) {
                HistorialElementoKafkaReq data = Formato.mapearObjDeserializado(kafkaRequest.getData(), HistorialElementoKafkaReq.class);
                // Inicio Proceso logico
                InfoHistorialElemento requestService = Formato.mapearObjDeserializado(data, InfoHistorialElemento.class);
                // Fin Proceso logico
                KafkaResponse<InfoHistorialElemento> response = new KafkaResponse<>();
                response.setData(Collections.singletonList(historialElementoService.guardarHistorialElemento(requestService)));
                commitKafka.acknowledge();
                log.info("Petición kafka sincrónico enviada: {}, Transacción: {}", kafkaRequest.getOp(), idTransKafka);
                return (KafkaResponse<T>) response;
            } else if (kafkaRequest.getOp().equalsIgnoreCase(CoreTecnicoConstants.OP_LISTA_HISTORIAL_ELEMENTO_POR_ELEMENTO)) {
                HistorialElementoKafkaReq data = Formato.mapearObjDeserializado(kafkaRequest.getData(), HistorialElementoKafkaReq.class);
                // Inicio Proceso logico
                HistorialElementoReqDTO requestService = Formato.mapearObjDeserializado(data, HistorialElementoReqDTO.class);
                // Fin Proceso logico
                KafkaResponse<InfoHistorialElemento> response = new KafkaResponse<>();
                response.setData(historialElementoService.listaHistorialElementoPorElemento(requestService));
                commitKafka.acknowledge();
                log.info("Petición kafka sincrónico enviada: {}, Transacción: {}", kafkaRequest.getOp(), idTransKafka);
                return (KafkaResponse<T>) response;
            } else if (kafkaRequest.getOp().equalsIgnoreCase(CoreTecnicoConstants.OP_LISTA_HISTORIAL_ELEMENTO_POR_FECHA)) {
                HistorialElementoKafkaReq data = Formato.mapearObjDeserializado(kafkaRequest.getData(), HistorialElementoKafkaReq.class);
                // Inicio Proceso logico
                HistorialElementoPorFechaReqDTO requestService = Formato.mapearObjDeserializado(data, HistorialElementoPorFechaReqDTO.class);
                // Fin Proceso logico
                KafkaResponse<InfoHistorialElemento> response = new KafkaResponse<>();
                response.setData(historialElementoService.listaHistorialElementoPorFecha(requestService));
                commitKafka.acknowledge();
                log.info("Petición kafka sincrónico enviada: {}, Transacción: {}", kafkaRequest.getOp(), idTransKafka);
                return (KafkaResponse<T>) response;
            } else if (kafkaRequest.getOp().equalsIgnoreCase(CoreTecnicoConstants.OP_LISTA_MARCA_ELEMENTO)) {
                KafkaResponse<AdmiMarcaElemento> response = new KafkaResponse<>();
                response.setData(marcaElementoService.listaMarcaElemento());
                commitKafka.acknowledge();
                log.info("Petición kafka sincrónico enviada: {}, Transacción: {}", kafkaRequest.getOp(), idTransKafka);
                return (KafkaResponse<T>) response;
            } else if (kafkaRequest.getOp().equalsIgnoreCase(CoreTecnicoConstants.OP_LISTA_MARCA_ELEMENTO_POR)) {
                MarcaElementoKafkaReq data = Formato.mapearObjDeserializado(kafkaRequest.getData(), MarcaElementoKafkaReq.class);
                // Inicio Proceso logico
                AdmiMarcaElemento requestService = Formato.mapearObjDeserializado(data, AdmiMarcaElemento.class);
                // Fin Proceso logico
                KafkaResponse<AdmiMarcaElemento> response = new KafkaResponse<>();
                response.setData(marcaElementoService.listaMarcaElementoPor(requestService));
                commitKafka.acknowledge();
                log.info("Petición kafka sincrónico enviada: {}, Transacción: {}", kafkaRequest.getOp(), idTransKafka);
                return (KafkaResponse<T>) response;
            } else if (kafkaRequest.getOp().equalsIgnoreCase(CoreTecnicoConstants.OP_GUARDAR_MODELO_ELEMENTO)) {
                ModeloElementoKafkaReq data = Formato.mapearObjDeserializado(kafkaRequest.getData(), ModeloElementoKafkaReq.class);
                // Inicio Proceso logico
                AdmiModeloElemento requestService = Formato.mapearObjDeserializado(data, AdmiModeloElemento.class);
                // Fin Proceso logico
                KafkaResponse<AdmiModeloElemento> response = new KafkaResponse<>();
                response.setData(Collections.singletonList(modeloElementoService.guardarModeloElemento(requestService)));
                commitKafka.acknowledge();
                log.info("Petición kafka sincrónico enviada: {}, Transacción: {}", kafkaRequest.getOp(), idTransKafka);
                return (KafkaResponse<T>) response;
            } else if (kafkaRequest.getOp().equalsIgnoreCase(CoreTecnicoConstants.OP_LISTA_MODELO_ELEMENTO)) {
                KafkaResponse<AdmiModeloElemento> response = new KafkaResponse<>();
                response.setData(modeloElementoService.listaModeloElemento());
                commitKafka.acknowledge();
                log.info("Petición kafka sincrónico enviada: {}, Transacción: {}", kafkaRequest.getOp(), idTransKafka);
                return (KafkaResponse<T>) response;
            } else if (kafkaRequest.getOp().equalsIgnoreCase(CoreTecnicoConstants.OP_LISTA_MODELO_ELEMENTO_POR)) {
                ModeloElementoKafkaReq data = Formato.mapearObjDeserializado(kafkaRequest.getData(), ModeloElementoKafkaReq.class);
                // Inicio Proceso logico
                AdmiModeloElemento requestService = Formato.mapearObjDeserializado(data, AdmiModeloElemento.class);
                // Fin Proceso logico
                KafkaResponse<AdmiModeloElemento> response = new KafkaResponse<>();
                response.setData(modeloElementoService.listaModeloElementoPor(requestService));
                commitKafka.acknowledge();
                log.info("Petición kafka sincrónico enviada: {}, Transacción: {}", kafkaRequest.getOp(), idTransKafka);
                return (KafkaResponse<T>) response;
            } else if (kafkaRequest.getOp().equalsIgnoreCase(CoreTecnicoConstants.OP_GUARDAR_TIPO_ELEMENTO)) {
                TipoElementoKafkaReq data = Formato.mapearObjDeserializado(kafkaRequest.getData(), TipoElementoKafkaReq.class);
                // Inicio Proceso logico
                AdmiTipoElemento requestService = Formato.mapearObjDeserializado(data, AdmiTipoElemento.class);
                // Fin Proceso logico
                KafkaResponse<AdmiTipoElemento> response = new KafkaResponse<>();
                response.setData(Collections.singletonList(tipoElementoService.guardarTipoElemento(requestService)));
                commitKafka.acknowledge();
                log.info("Petición kafka sincrónico enviada: {}, Transacción: {}", kafkaRequest.getOp(), idTransKafka);
                return (KafkaResponse<T>) response;
            } else if (kafkaRequest.getOp().equalsIgnoreCase(CoreTecnicoConstants.OP_ACTUALIZAR_TIPO_ELEMENTO)) {
                TipoElementoKafkaReq data = Formato.mapearObjDeserializado(kafkaRequest.getData(), TipoElementoKafkaReq.class);
                // Inicio Proceso logico
                AdmiTipoElemento requestService = Formato.mapearObjDeserializado(data, AdmiTipoElemento.class);
                // Fin Proceso logico
                KafkaResponse<AdmiTipoElemento> response = new KafkaResponse<>();
                response.setData(Collections.singletonList(tipoElementoService.actualizarTipoElemento(requestService)));
                commitKafka.acknowledge();
                log.info("Petición kafka sincrónico enviada: {}, Transacción: {}", kafkaRequest.getOp(), idTransKafka);
                return (KafkaResponse<T>) response;
            } else if (kafkaRequest.getOp().equalsIgnoreCase(CoreTecnicoConstants.OP_ELIMINAR_TIPO_ELEMENTO)) {
                TipoElementoKafkaReq data = Formato.mapearObjDeserializado(kafkaRequest.getData(), TipoElementoKafkaReq.class);
                // Inicio Proceso logico
                AdmiTipoElemento requestService = Formato.mapearObjDeserializado(data, AdmiTipoElemento.class);
                // Fin Proceso logico
                KafkaResponse<Boolean> response = new KafkaResponse<>();
                response.setData(Collections.singletonList(tipoElementoService.eliminarTipoElemento(requestService)));
                commitKafka.acknowledge();
                log.info("Petición kafka sincrónico enviada: {}, Transacción: {}", kafkaRequest.getOp(), idTransKafka);
                return (KafkaResponse<T>) response;
            } else if (kafkaRequest.getOp().equalsIgnoreCase(CoreTecnicoConstants.OP_LISTA_TIPO_ELEMENTO)) {
                KafkaResponse<AdmiTipoElemento> response = new KafkaResponse<>();
                response.setData(tipoElementoService.listaTipoElemento());
                commitKafka.acknowledge();
                log.info("Petición kafka sincrónico enviada: {}, Transacción: {}", kafkaRequest.getOp(), idTransKafka);
                return (KafkaResponse<T>) response;
            } else if (kafkaRequest.getOp().equalsIgnoreCase(CoreTecnicoConstants.OP_LISTA_TIPO_ELEMENTO_POR)) {
                TipoElementoKafkaReq data = Formato.mapearObjDeserializado(kafkaRequest.getData(), TipoElementoKafkaReq.class);
                // Inicio Proceso logico
                AdmiTipoElemento requestService = Formato.mapearObjDeserializado(data, AdmiTipoElemento.class);
                // Fin Proceso logico
                KafkaResponse<AdmiTipoElemento> response = new KafkaResponse<>();
                response.setData(tipoElementoService.listaTipoElementoPor(requestService));
                commitKafka.acknowledge();
                log.info("Petición kafka sincrónico enviada: {}, Transacción: {}", kafkaRequest.getOp(), idTransKafka);
                return (KafkaResponse<T>) response;
            } else if (kafkaRequest.getOp().equalsIgnoreCase(CoreTecnicoConstants.OP_LISTA_DATOS_VEHICULO)) {
                DatosVehiculoKafkaReq data = Formato.mapearObjDeserializado(kafkaRequest.getData(), DatosVehiculoKafkaReq.class);
                // Inicio Proceso logico
                DatosVehiculoReqDTO requestService = Formato.mapearObjDeserializado(data, DatosVehiculoReqDTO.class);
                // Fin Proceso logico
                KafkaResponse<DatosVehiculoResDTO> response = new KafkaResponse<>();
                response.setData(consultasService.datosVehiculo(requestService));
                commitKafka.acknowledge();
                log.info("Petición kafka sincrónico enviada: {}, Transacción: {}", kafkaRequest.getOp(), idTransKafka);
                return (KafkaResponse<T>) response;
            } else if (kafkaRequest.getOp().equalsIgnoreCase(CoreTecnicoConstants.OP_LISTA_ELEMENTO_POR_GRUPO)) {
                ElementoPorGrupoKafkaReq data = Formato.mapearObjDeserializado(kafkaRequest.getData(), ElementoPorGrupoKafkaReq.class);
                // Inicio Proceso logico
                ElementoPorGrupoReqDTO requestService = Formato.mapearObjDeserializado(data, ElementoPorGrupoReqDTO.class);
                // Fin Proceso logico
                KafkaResponse<ElementoPorGrupoResDTO> response = new KafkaResponse<>();
                response.setData(consultasService.elementoPorGrupo(requestService));
                commitKafka.acknowledge();
                log.info("Petición kafka sincrónico enviada: {}, Transacción: {}", kafkaRequest.getOp(), idTransKafka);
                return (KafkaResponse<T>) response;
            } else if (kafkaRequest.getOp().equalsIgnoreCase(CoreTecnicoConstants.OP_LISTA_ELEMENTO_POR_DEPARTAMENTO_PARAMS)) {
                ElementoKafkaReq data = Formato.mapearObjDeserializado(kafkaRequest.getData(), ElementoKafkaReq.class);
                // Inicio Proceso logico
                ElementoPorDepartamentoParamsReqDTO requestService = Formato.mapearObjDeserializado(data, ElementoPorDepartamentoParamsReqDTO.class);
                // Fin Proceso logico
                KafkaResponse<InfoElemento> response = new KafkaResponse<>();
                response.setData(elementoService.listaElementoPorDepartamentoParams(requestService));
                commitKafka.acknowledge();
                log.info("Petición kafka sincrónico enviada: {}, Transacción: {}", kafkaRequest.getOp(), idTransKafka);
                return (KafkaResponse<T>) response;
            } else if (kafkaRequest.getOp().equalsIgnoreCase(CoreTecnicoConstants.OP_ASIGNAR_UBICACION_ELEMENTO)) {
                UbicacionElementokafkaReq data = Formato.mapearObjDeserializado(kafkaRequest.getData(), UbicacionElementokafkaReq.class);
                // Inicio Proceso logico
                UbicacionElementoReqDTO requestService = Formato.mapearObjDeserializado(data, UbicacionElementoReqDTO.class);
                // Fin Proceso logico
                KafkaResponse<String> response = new KafkaResponse<>();
                response.setData(Collections.singletonList(transaccionesService.asignarUbicacionElemento(requestService)));
                commitKafka.acknowledge();
                log.info("Petición kafka sincrónico enviada: {}, Transacción: {}", kafkaRequest.getOp(), idTransKafka);
                return (KafkaResponse<T>) response;
            } else if (kafkaRequest.getOp().equalsIgnoreCase(CoreTecnicoConstants.OP_MODIFICAR_UBICACION_ELEMENTO)) {
                UbicacionElementokafkaReq data = Formato.mapearObjDeserializado(kafkaRequest.getData(), UbicacionElementokafkaReq.class);
                // Inicio Proceso logico
                UbicacionElementoReqDTO requestService = Formato.mapearObjDeserializado(data, UbicacionElementoReqDTO.class);
                // Fin Proceso logico
                KafkaResponse<String> response = new KafkaResponse<>();
                response.setData(Collections.singletonList(transaccionesService.modificarUbicacionElemento(requestService)));
                commitKafka.acknowledge();
                log.info("Petición kafka sincrónico enviada: {}, Transacción: {}", kafkaRequest.getOp(), idTransKafka);
                return (KafkaResponse<T>) response;
            } else if (kafkaRequest.getOp().equalsIgnoreCase(CoreTecnicoConstants.OP_LISTA_ELEMENTO_POR_CUADRILLA_PARAMS)) {
                ElementoKafkaReq data = Formato.mapearObjDeserializado(kafkaRequest.getData(), ElementoKafkaReq.class);
                // Inicio Proceso logico
                ElementoPorCuadrillaParamsReqDTO requestService = Formato.mapearObjDeserializado(data, ElementoPorCuadrillaParamsReqDTO.class);
                // Fin Proceso logico
                KafkaResponse<InfoElemento> response = new KafkaResponse<>();
                response.setData(elementoService.listaElementoPorCuadrillaParams(requestService));
                commitKafka.acknowledge();
                log.info("Petición kafka sincrónico enviada: {}, Transacción: {}", kafkaRequest.getOp(), idTransKafka);
                return (KafkaResponse<T>) response;
            } else if (kafkaRequest.getOp().equalsIgnoreCase(CoreTecnicoConstants.OP_MODELOS_ELEM_MONITORIZADOS)) {
                ModelosElemMonitorizadoskafkaReq data = Formato.mapearObjDeserializado(kafkaRequest.getData(), ModelosElemMonitorizadoskafkaReq.class);
                // Inicio Proceso logico
                ModelosElemMonitorizadosReqDTO requestService = Formato.mapearObjDeserializado(data, ModelosElemMonitorizadosReqDTO.class);
                // Fin Proceso logico
                KafkaResponse<ModelosElemMonitorizadosResDTO> response = new KafkaResponse<>();
                response.setData(consultasService.modelosElemMonitorizados(requestService));
                commitKafka.acknowledge();
                log.info("Petición kafka sincrónico enviada: {}, Transacción: {}", kafkaRequest.getOp(), idTransKafka);
                return (KafkaResponse<T>) response;
            } else {
                kafkaResponse.setCode(500);
                kafkaResponse.setStatus("ERROR");
                kafkaResponse.setMessage(
                        "No se encuentra configurado el OP " + kafkaRequest.getOp() + " en el grupo " + CoreTecnicoConstants.GROUP_ELEMENTO);
            }
        } catch (GenericException e) {
            kafkaResponse.setCode(e.getCodeError());
            kafkaResponse.setStatus(e.getStatusError());
            kafkaResponse.setMessage(e.getMessageError());
        } catch (Exception e) {
            kafkaResponse.setCode(100);
            kafkaResponse.setStatus("ERROR");
            kafkaResponse.setMessage(e.getMessage());
        }
        commitKafka.acknowledge();
        log.info("Petición kafka sincrónico enviada: {}, Transacción: {}, Estado: Fallida", kafkaRequest.getOp(), idTransKafka);
        return (KafkaResponse<T>) kafkaResponse;
    }

    private void validateConsumerMessage(String dateSendMessage) throws GenericException {
        if (!dateSendMessage.equals("")) {
            Date dateMessageReceived = Formato.getDateByString(dateSendMessage, "yyyy-MM-dd HH:mm:ss");
            Date dateMessageProcess = new Date();
            long segReplyTimeout = Long.parseLong(replyTimeout.replace("s", "").trim());
            long segDiffDateMessage = TimeUnit.MILLISECONDS.toSeconds(dateMessageProcess.getTime() - dateMessageReceived.getTime());
            if (segDiffDateMessage > segReplyTimeout) {
                String msg = "El mensaje sé ha ignorado porque ha sobrepasado el tiempo de lectura (" + segDiffDateMessage + " segundos) del consumidor sincrónico " + "(" + segReplyTimeout + " segundos)";
                log.error(msg);
                throw new GenericException(msg);
            }
        }
    }
}