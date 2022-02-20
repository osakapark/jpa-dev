package com.mystudy.event;

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

}
