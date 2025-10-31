package co.unicauca.comunicacionmicroservicios.service;

import co.unicauca.comunicacionmicroservicios.dto.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.Optional;

public interface ISubmissionService {

    // FORMATO A
    IdResponse crearFormatoA(String userId, FormatoAData data, MultipartFile pdf, MultipartFile carta);
    FormatoAView obtenerFormatoA(Long id);
    FormatoAPage listarFormatoA(Optional<String> docenteId, int page, int size);
    IdResponse reenviarFormatoA(String userId, Long proyectoId, MultipartFile pdf, MultipartFile carta);
    void cambiarEstadoFormatoA(Long versionId, EvaluacionRequest req);

    // ANTEPROYECTO
    IdResponse subirAnteproyecto(String userId, AnteproyectoData data, MultipartFile pdf);
    AnteproyectoPage listarAnteproyectos(int page, int size);
    void cambiarEstadoAnteproyecto(Long id, CambioEstadoAnteproyectoRequest req);
}
