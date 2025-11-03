package co.unicauca.gestiontrabajogrado.infrastructure.repository;

import co.unicauca.gestiontrabajogrado.dto.FormatoADetalleDTO;

import java.util.List;

/**
 * Interfaz para repositorio de formatos A
 */
public interface IFormatoARepository {
    List<FormatoADetalleDTO> findAll();
    FormatoADetalleDTO findById(Long id);
    FormatoADetalleDTO save(FormatoADetalleDTO formato);
}

