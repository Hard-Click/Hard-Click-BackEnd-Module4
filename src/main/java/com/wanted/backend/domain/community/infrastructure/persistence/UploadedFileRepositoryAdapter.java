package com.wanted.backend.domain.community.infrastructure.persistence;

import com.wanted.backend.domain.community.domain.model.UploadedFile;
import com.wanted.backend.domain.community.domain.repository.UploadedFileRepository;
import org.springframework.stereotype.Repository;

@Repository
public class UploadedFileRepositoryAdapter implements UploadedFileRepository {

    private final SpringDataUploadedFileRepository repository;

    public UploadedFileRepositoryAdapter(SpringDataUploadedFileRepository repository) {
        this.repository = repository;
    }

    @Override
    public UploadedFile save(UploadedFile uploadedFile) {
        UploadedFileJpaEntity entity = new UploadedFileJpaEntity(
                uploadedFile.getUploaderId(),
                uploadedFile.getOriginalName(),
                uploadedFile.getFileUrl(),
                uploadedFile.getFileType(),
                uploadedFile.getFileSize(),
                uploadedFile.getCreatedAt()
        );
        return toDomain(repository.save(entity));
    }

    private UploadedFile toDomain(UploadedFileJpaEntity entity) {
        return UploadedFile.restore(
                entity.getId(),
                entity.getUploaderId(),
                entity.getOriginalName(),
                entity.getFileUrl(),
                entity.getFileType(),
                entity.getFileSize(),
                entity.getCreatedAt()
        );
    }
}