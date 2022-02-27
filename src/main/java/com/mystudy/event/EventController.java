package com.mystudy.event;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import javax.validation.Valid;

import org.modelmapper.ModelMapper;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.Errors;
import org.springframework.web.bind.WebDataBinder;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.InitBinder;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import com.mystudy.domain.Event;
import com.mystudy.domain.Member;
import com.mystudy.domain.Study;
import com.mystudy.event.form.EventForm;
import com.mystudy.event.validator.EventValidator;
import com.mystudy.member.CurrentMember;
import com.mystudy.study.StudyRepository;
import com.mystudy.study.StudyService;

import lombok.RequiredArgsConstructor;

@Controller
@RequestMapping("/study/{path}")
@RequiredArgsConstructor
public class EventController {

	private final StudyService studyService;
	private final EventService eventService;
	private final ModelMapper modelMapper;
	private final EventValidator eventValidator;
	private final EventRepository eventRepository;
	private final StudyRepository studyRepository;

	@InitBinder("eventForm")
	public void initBinder(WebDataBinder webDataBinder) {
		webDataBinder.addValidators(eventValidator);
	}

	@GetMapping("/new-event")
	public String newEventForm(@CurrentMember Member member, @PathVariable String path, Model model) {
		Study study = studyService.getStudyToUpdate(member, path);
		model.addAttribute(study);
		model.addAttribute(member);
		model.addAttribute(new EventForm());
		return "event/form";
	}

	@PostMapping("/new-event")
	public String newEventSubmit(@CurrentMember Member member, @PathVariable String path, @Valid EventForm eventForm,
			Errors errors, Model model) {
		Study study = studyService.getStudyToUpdate(member, path);
		if (errors.hasErrors()) {
			model.addAttribute(member);
			model.addAttribute(study);
			return "event/form";
		}

		Event event = eventService.createEvent(modelMapper.map(eventForm, Event.class), study, member);
		return "redirect:/study/" + study.getEncodedPath() + "/events/" + event.getId();
	}

	@GetMapping("/events/{id}")
	public String getEvent(@CurrentMember Member member, @PathVariable String path, @PathVariable Long id,
			Model model) {
		model.addAttribute(member);
		model.addAttribute(eventRepository.findById(id).orElseThrow());
		model.addAttribute(studyRepository.findStudyWithManagersByPath(path));
		return "event/view";
	}

	@GetMapping("/events")
	public String viewStudyEvents(@CurrentMember Member member, @PathVariable String path, Model model) {
		Study study = studyService.getStudy(path);
		model.addAttribute(member);
		model.addAttribute(study);

		List<Event> events = eventRepository.findByStudyOrderByStartDateTime(study);
		List<Event> newEvents = new ArrayList<>();
		List<Event> oldEvents = new ArrayList<>();

		events.forEach(e -> {
			if (e.getEndDateTime().isBefore(LocalDateTime.now())) {
				oldEvents.add(e);
			} else {
				newEvents.add(e);
			}
		});

		model.addAttribute("newEvents", newEvents);
		model.addAttribute("oldEvents", oldEvents);

		return "study/events";
	}

	@GetMapping("/events/{id}/edit")
	public String updateEventForm(@CurrentMember Member member, @PathVariable String path, @PathVariable Long id,
			Model model) {

		Study study = studyService.getStudyToUpdate(member, path);
		Event event = eventRepository.findById(id).orElseThrow();
		model.addAttribute(study);
		model.addAttribute(member);
		model.addAttribute(event);
		model.addAttribute(modelMapper.map(event, EventForm.class));
		return "event/update-form";
	}

	@PostMapping("/events/{id}/edit")
	public String updateEventSubmit(@CurrentMember Member member, @PathVariable String path, @PathVariable Long id,
			@Valid EventForm eventForm, Errors errors, Model model) {

		Study study = studyService.getStudyToUpdate(member, path);
		Event event = eventRepository.findById(id).orElseThrow();
		eventForm.setEventType(event.getEventType());
		eventValidator.validateUpdateForm(eventForm, event, errors);

		if (errors.hasErrors()) {
			model.addAttribute(member);
			model.addAttribute(study);
			model.addAttribute(event);
			return "event/update-form";
		}

		eventService.updateEvent(event, eventForm);
		return "redirect:/study/" + study.getEncodedPath() + "/events/" + event.getId();
	}

}
