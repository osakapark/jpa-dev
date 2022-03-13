package com.mystudy.modules.tag;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import lombok.RequiredArgsConstructor;

@Service
@Transactional
@RequiredArgsConstructor
public class TagService {
	private final TagRepository tagRepository;

	public Tag findOrCreateNew(String tagTitle) {
		Tag tag = tagRepository.findByTitle(tagTitle);
		if (tag == null) {
			tag = tagRepository.save(Tag.builder().title(tagTitle).build());
		}
		return tag;
	}
}