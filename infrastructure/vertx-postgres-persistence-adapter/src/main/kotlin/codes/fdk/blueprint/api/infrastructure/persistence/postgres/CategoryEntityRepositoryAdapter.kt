package codes.fdk.blueprint.api.infrastructure.persistence.postgres

import codes.fdk.blueprint.api.domain.model.Category
import codes.fdk.blueprint.api.domain.model.CategoryId
import codes.fdk.blueprint.api.domain.spi.CategoryRepository
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.reactor.flux
import kotlinx.coroutines.reactor.mono
import reactor.core.publisher.Flux
import reactor.core.publisher.Mono

class CategoryEntityRepositoryAdapter(private val categoryEntityRepository: CategoryEntityRepository) : CategoryRepository {

    private val categoryEntityMapper = CategoryEntityMapper.INSTANCE;

    override fun save(category: Category): Mono<Category> {
        return mono { categoryEntityRepository.save(categoryEntityMapper.toEntity(category)) }
            .map(categoryEntityMapper::fromEntity)
    }

    override fun findAll(): Flux<Category> {
        return flux {
            categoryEntityRepository.findAll().collect { send(it) }
        }.map(categoryEntityMapper::fromEntity)
    }

    override fun findById(id: CategoryId): Mono<Category> {
        return mono { categoryEntityRepository.findById(id) }
            .map(categoryEntityMapper::fromEntity)
    }

    override fun findByParentId(parentId: CategoryId): Flux<Category> {
        return flux {
            categoryEntityRepository.findByParentId(parentId).collect { send(it) }
        }.map(categoryEntityMapper::fromEntity)
    }

}