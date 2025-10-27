package co.unicauca.review.repository;

import co.unicauca.review.entity.Evaluation;
import co.unicauca.review.enums.DocumentType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface EvaluationRepository extends JpaRepository<Evaluation, Long> {

    List<Evaluation> findByDocumentIdAndDocumentType(Long documentId, DocumentType documentType);

    Page<Evaluation> findByEvaluatorId(Long evaluatorId, Pageable pageable);

    Page<Evaluation> findByDocumentType(DocumentType documentType, Pageable pageable);

    boolean existsByDocumentIdAndDocumentTypeAndEvaluatorId(
            Long documentId, DocumentType documentType, Long evaluatorId);
}
