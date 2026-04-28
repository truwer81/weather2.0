package com.example.weather.location;

import com.example.weather.location.dto.LocationDTO;
import org.mapstruct.Mapper;

import java.util.List;

@Mapper(componentModel = "spring")
public interface LocationMapper {

    LocationDTO toDTO(Location location);

    List<LocationDTO> toDTOList(List<Location> locations);
}
