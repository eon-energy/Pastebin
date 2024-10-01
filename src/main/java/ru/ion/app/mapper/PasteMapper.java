package ru.ion.app.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.ReportingPolicy;
import ru.ion.app.DTO.PasteData;
import ru.ion.app.entitys.Paste;

import java.time.LocalDate;

@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface PasteMapper {

    @Mapping(target = "createDate", source = "localDate")
    @Mapping(target = "endDate", source = "pasteData.endDate")
    @Mapping(target = "key", source = "generatedKey")
    Paste toPaste(PasteData pasteData, String generatedKey, LocalDate localDate);

}
