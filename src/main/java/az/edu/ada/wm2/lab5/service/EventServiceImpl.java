package az.edu.ada.wm2.lab5.service;

import az.edu.ada.wm2.lab5.model.Event;
import az.edu.ada.wm2.lab5.repository.EventRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Comparator;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
public class EventServiceImpl implements EventService {

    private final EventRepository eventRepository;

    @Autowired
    public EventServiceImpl(EventRepository eventRepository) {
        this.eventRepository = eventRepository;
    }

    @Override
    public Event createEvent(Event event) {
        if (event.getId() == null) {
            event.setId(UUID.randomUUID());
        }
        return eventRepository.save(event);
    }

    @Override
    public Event getEventById(UUID id) {
        return eventRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Event not found with id: " + id));
    }

    @Override
    public List<Event> getAllEvents() {
        return eventRepository.findAll();
    }

    @Override
    public Event updateEvent(UUID id, Event event) {
        if (!eventRepository.existsById(id)) {
            throw new RuntimeException("Event not found with id: " + id);
        }
        event.setId(id);
        return eventRepository.save(event);
    }

    @Override
    public void deleteEvent(UUID id) {
        if (!eventRepository.existsById(id)) {
            throw new RuntimeException("Event not found with id: " + id);
        }
        eventRepository.deleteById(id);
    }

    @Override
    public Event partialUpdateEvent(UUID id, Event partialEvent) {
        Event existingEvent = eventRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Event not found with id: " + id));

        if (partialEvent.getEventName() != null) {
            existingEvent.setEventName(partialEvent.getEventName());
        }
        if (partialEvent.getTags() != null && !partialEvent.getTags().isEmpty()) {
            existingEvent.setTags(partialEvent.getTags());
        }
        if (partialEvent.getTicketPrice() != null) {
            existingEvent.setTicketPrice(partialEvent.getTicketPrice());
        }
        if (partialEvent.getEventDateTime() != null) {
            existingEvent.setEventDateTime(partialEvent.getEventDateTime());
        }
        if (partialEvent.getDurationMinutes() > 0) {
            existingEvent.setDurationMinutes(partialEvent.getDurationMinutes());
        }

        return eventRepository.save(existingEvent);
    }

    @Override
    public List<Event> getEventsByTag(String tag) {
        if (tag == null || tag.trim().isEmpty()) {
            throw new IllegalArgumentException("Tag cannot be null or blank");
        }

        List<Event> all = eventRepository.findAll();
        if (all == null || all.isEmpty()) {
            return List.of();
        }

        String normalizedTag = tag.trim().toLowerCase();

        return all.stream()
                .filter(e -> e != null && e.getTags() != null)
                .filter(e -> e.getTags().stream()
                        .filter(t -> t != null && !t.trim().isEmpty())
                        .map(t -> t.trim().toLowerCase())
                        .anyMatch(t -> t.equals(normalizedTag)))
                .sorted(Comparator.comparing(Event::getEventDateTime,
                        Comparator.nullsLast(Comparator.naturalOrder())))
                .collect(Collectors.toList());
    }

    @Override
    public List<Event> getUpcomingEvents() {
        List<Event> all = eventRepository.findAll();
        if (all == null || all.isEmpty()) {
            return List.of();
        }

        LocalDateTime now = LocalDateTime.now();

        return all.stream()
                .filter(e -> e != null && e.getEventDateTime() != null)
                .filter(e -> e.getEventDateTime().isAfter(now))
                .sorted(Comparator.comparing(Event::getEventDateTime))
                .collect(Collectors.toList());
    }

    @Override
    public List<Event> getEventsByPriceRange(BigDecimal minPrice, BigDecimal maxPrice) {
        if (minPrice == null || maxPrice == null) {
            throw new IllegalArgumentException("minPrice and maxPrice cannot be null");
        }
        if (minPrice.compareTo(BigDecimal.ZERO) < 0 || maxPrice.compareTo(BigDecimal.ZERO) < 0) {
            throw new IllegalArgumentException("Price cannot be negative");
        }
        if (minPrice.compareTo(maxPrice) > 0) {
            throw new IllegalArgumentException("minPrice cannot be greater than maxPrice");
        }

        List<Event> all = eventRepository.findAll();
        if (all == null || all.isEmpty()) {
            return List.of();
        }

        return all.stream()
                .filter(e -> e != null && e.getTicketPrice() != null)
                .filter(e -> e.getTicketPrice().compareTo(minPrice) >= 0
                        && e.getTicketPrice().compareTo(maxPrice) <= 0)
                .sorted(Comparator.comparing(Event::getTicketPrice)
                        .thenComparing(Event::getEventDateTime, Comparator.nullsLast(Comparator.naturalOrder())))
                .collect(Collectors.toList());
    }

    @Override
    public List<Event> getEventsByDateRange(LocalDateTime start, LocalDateTime end) {
        if (start == null || end == null) {
            throw new IllegalArgumentException("start and end cannot be null");
        }
        if (start.isAfter(end)) {
            throw new IllegalArgumentException("start cannot be after end");
        }

        List<Event> all = eventRepository.findAll();
        if (all == null || all.isEmpty()) {
            return List.of();
        }

        return all.stream()
                .filter(e -> e != null && e.getEventDateTime() != null)

                .filter(e -> !e.getEventDateTime().isBefore(start) && !e.getEventDateTime().isAfter(end))
                .sorted(Comparator.comparing(Event::getEventDateTime))
                .collect(Collectors.toList());
    }

    @Override
    public Event updateEventPrice(UUID id, BigDecimal newPrice) {
        if (id == null) {
            throw new IllegalArgumentException("Event id cannot be null");
        }
        if (newPrice == null) {
            throw new IllegalArgumentException("New price cannot be null");
        }
        if (newPrice.compareTo(BigDecimal.ZERO) < 0) {
            throw new IllegalArgumentException("New price cannot be negative");
        }

        Event existingEvent = eventRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Event not found with id: " + id));

        existingEvent.setTicketPrice(newPrice);
        return eventRepository.save(existingEvent);
    }
}