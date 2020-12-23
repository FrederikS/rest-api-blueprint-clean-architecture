package codes.fdk.blueprint.api.domain.service;

import codes.fdk.blueprint.api.domain.command.CreateCategoryCommand;
import codes.fdk.blueprint.api.domain.command.UpdateCategoryCommand;
import codes.fdk.blueprint.api.domain.model.Category;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.factory.Mappers;

@Mapper
interface CategoryMapper {

    CategoryMapper INSTANCE = Mappers.getMapper(CategoryMapper.class);

    Category toCategory(CreateCategoryCommand command);

    @Mapping(source = "command.visible", target = "visible")
    @Mapping(source = "category.id", target = "id")
    Category updateCategory(Category category, UpdateCategoryCommand command);

}
