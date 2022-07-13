package nextstep.subway.applicaion;

import java.util.List;
import java.util.stream.Collectors;
import nextstep.subway.applicaion.dto.LineRequest;
import nextstep.subway.applicaion.dto.LineResponse;
import nextstep.subway.applicaion.exception.ExceptionMessages;
import nextstep.subway.domain.Line;
import nextstep.subway.domain.LineRepository;
import nextstep.subway.domain.Section;
import nextstep.subway.domain.SectionRepository;
import nextstep.subway.domain.Station;
import nextstep.subway.domain.StationRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class LineService {

    private final LineRepository lineRepository;
    private final StationRepository stationRepository;
    private final SectionRepository sectionRepository;

    public LineService(LineRepository lineRepository, StationRepository stationRepository,
        SectionRepository sectionRepository) {
        this.lineRepository = lineRepository;
        this.stationRepository = stationRepository;
        this.sectionRepository = sectionRepository;
    }

    @Transactional
    public LineResponse saveLine(LineRequest lineRequest) {
        Line line = lineRepository.save(lineRequest.toEntity());
        saveEndpoints(lineRequest, line);
        Line savedLine = lineRepository.findById(line.getId())
            .orElseThrow(() -> new RuntimeException(ExceptionMessages.getNoLineExceptionMessage(line.getId())));
        saveSection(savedLine,lineRequest.getDistance());
        return LineResponse.convertedByEntity(savedLine);
    }


    public List<LineResponse> getLines() {
        List<Line> lines = lineRepository.findAll();
        return lines.stream().map(LineResponse::convertedByEntity).collect(Collectors.toList());
    }

    public LineResponse getLine(long lineId) {
        Line line = lineRepository.findById(lineId)
            .orElseThrow(() -> new RuntimeException(ExceptionMessages.getNoLineExceptionMessage(lineId)));
        return LineResponse.convertedByEntity(line);
    }

    @Transactional
    public LineResponse updateLine(LineRequest lineRequest, long lineId) {
        Line line = lineRepository.findById(lineId)
            .orElseThrow(() -> new RuntimeException(ExceptionMessages.getNoLineExceptionMessage(lineId)));
        line.changeNameAndColor(lineRequest.getName(), lineRequest.getColor());
        Line updatedLine = lineRepository.save(line);
        return LineResponse.convertedByEntity(updatedLine);
    }

    @Transactional
    public void deleteLine(long lineId) {
        lineRepository.deleteById(lineId);
    }


    private void saveEndpoints(LineRequest lineRequest, Line savedLine) {
        Station upStation = getStation(lineRequest.getUpStationId());
        Station downStation = getStation(lineRequest.getDownStationId());
        savedLine.addEndpointStation(upStation, downStation);
        lineRepository.save(savedLine);
    }

    private Station getStation(long stationId) {
        return stationRepository.findById(stationId)
            .orElseThrow(() -> new RuntimeException(ExceptionMessages.getNoStationExceptionMessage(stationId)));
    }

    private void saveSection(Line line, long distance) {
        Station upStation = getStation(line.getUpEndpoint().getId());
        Station downStation = getStation(line.getDownEndpoint().getId());
        Section section = new Section(line, upStation, downStation, distance);
        sectionRepository.save(section);
    }

}
