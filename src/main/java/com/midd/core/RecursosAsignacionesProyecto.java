package com.midd.core;

import java.util.List;

import com.midd.core.administracion.ServicosAsignacionProyecto;
import com.midd.core.modelo.AsignacionProyecto;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import org.springframework.web.bind.annotation.RequestMethod;

import com.midd.core.Respuestas.Respuestas;

@RestController
@CrossOrigin(origins = "*", methods = { RequestMethod.GET, RequestMethod.POST })
@RequestMapping("/asignaciones-proyectos")
public class RecursosAsignacionesProyecto {
        private final ServicosAsignacionProyecto servicio_asignaciones;
        private final Respuestas respuestas;

        Logger logger = LoggerFactory.getLogger(RecursosAsignacionesProyecto.class);

        @Autowired
        public RecursosAsignacionesProyecto(ServicosAsignacionProyecto servicio_asignaciones, Respuestas respuestas) {
                this.servicio_asignaciones = servicio_asignaciones;
                this.respuestas = respuestas;
        }

        @PostMapping("/agregar-asignacion-proyecto")
        public ResponseEntity<?> agregarAsignacionProyecto(@RequestBody AsignacionProyecto asignacion_proyecto) {
                if (servicio_asignaciones.validarMiembro(asignacion_proyecto.getUltimatix_asi(),
                                asignacion_proyecto.getId_equipo_asi())) {
                        
                        logger.warn("Usuario " + asignacion_proyecto.getUltimatix_asi()
                                        + " ya se encuentra registrado en el proyecto "
                                        + asignacion_proyecto.getId_equipo_asi());
                        return new ResponseEntity<>(respuestas.respuestas("Usuario ya se encuentra resgistrado en este proyecto", "3000"), HttpStatus.BAD_REQUEST);
                }
                if (servicio_asignaciones.validarAsignacion(asignacion_proyecto.getUltimatix_asi(),
                                asignacion_proyecto.getAsignacion())){
                        logger.warn("Usuario " + asignacion_proyecto.getUltimatix_asi()
                                        + " no puede tener una asignacion superior al 100%");
                        return new ResponseEntity<>(respuestas.respuestas("El usuario no puede tener una asignación superior al 100%", "3000"), HttpStatus.BAD_REQUEST);
                }
                if (asignacion_proyecto.getFecha_inicio().after(asignacion_proyecto.getFecha_fin())) {
                        logger.warn("Fecha inicio no puede ser mayor que la fecha fin");
                        return new ResponseEntity<>(respuestas.respuestas("Fecha inicio no puede ser mayor que la fecha fin", "3000"), HttpStatus.BAD_REQUEST);
                }
                servicio_asignaciones.agregarMiembroEquipo(asignacion_proyecto.getUltimatix_asi(),
                                asignacion_proyecto.getId_equipo_asi(), asignacion_proyecto.getAsignacion());
                asignacion_proyecto.setEstado(true);
                servicio_asignaciones.agregarAsignacionProyecto(asignacion_proyecto);
                return new ResponseEntity<>(asignacion_proyecto, HttpStatus.OK);
        }

        @PostMapping("/actualizar-Fecha-fin")
        public ResponseEntity<?> actualizarFechaFinAsignacionProyecto(
                        @RequestBody AsignacionProyecto asignacion_proyecto) {
                try {
                        AsignacionProyecto mi = servicio_asignaciones.buscarAsigancionProyectoId(
                                        asignacion_proyecto.getId_asignacion_proyecto_asg());
                        if (mi.getFecha_inicio().after(asignacion_proyecto.getFecha_fin())) {
                                logger.warn("Fecha inicio no puede ser mayor que la fecha fin");
                                return new ResponseEntity<>(respuestas.respuestas("Fecha inicio no puede ser mayor que la fecha fin", "3000"), HttpStatus.BAD_REQUEST);
                        }
                        mi.setFecha_fin(asignacion_proyecto.getFecha_fin());
                        servicio_asignaciones.agregarAsignacionProyecto(mi);
                        return new ResponseEntity<>(mi, HttpStatus.OK);

                } catch (Exception e) {
                        logger.warn("La asignación " + asignacion_proyecto.getId_asignacion_proyecto_asg()
                                        + " no se encuentra registrada");
                        return new ResponseEntity<>(respuestas.respuestas("Asignación no registrada", "2021"), HttpStatus.BAD_REQUEST);
                }
        }

        @PostMapping("/dar-baja")
        public ResponseEntity<?> actualizarFechaDeBaja(
                        @RequestBody AsignacionProyecto asignacion_proyecto) {
                try {
                        AsignacionProyecto mi = servicio_asignaciones.buscarAsigancionProyectoId(
                                        asignacion_proyecto.getId_asignacion_proyecto_asg());
                        if (mi.getFecha_inicio().after(asignacion_proyecto.getFecha_baja())) {
                                logger.warn("Fecha inicio no puede ser mayor que la fecha fin");
                                return new ResponseEntity<>(respuestas.respuestas("Fecha inicio no puede ser mayor que la fecha de baja", "3000"), HttpStatus.BAD_REQUEST);
                        }
                        mi.setFecha_baja(asignacion_proyecto.getFecha_baja());
                        mi.setEstado(false);
                        servicio_asignaciones.restarAsignacion(mi.getUltimatix_asi(),
                                        mi.getAsignacion());
                        servicio_asignaciones.quitarMiembroEquipo(mi.getUltimatix_asi(),
                                        mi.getId_equipo_asi());
                        servicio_asignaciones.agregarAsignacionProyecto(mi);
                        return new ResponseEntity<>(mi, HttpStatus.OK);

                } catch (Exception e) {
                        logger.warn("La asignación "+ asignacion_proyecto.getId_asignacion_proyecto_asg()+" no se encuentra registrada");
                        return new ResponseEntity<>(respuestas.respuestas("Asignación no registrada", "2021"), HttpStatus.BAD_REQUEST);
                }
        }

        @PostMapping("/obtener-asignaciones")
        public ResponseEntity<?> obtenerAsignaciones(@RequestBody AsignacionProyecto asignacion_proyecto) {
                List<AsignacionProyecto> listaUltimatix = servicio_asignaciones
                                .buscarAsignacionUltimatix(asignacion_proyecto.getUltimatix_asi());
                return new ResponseEntity<>(listaUltimatix, HttpStatus.OK);
        }

}
