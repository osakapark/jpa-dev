package com.mystudy.modules.tag;

import org.springframework.transaction.annotation.Transactional;
import org.springframework.data.jpa.repository.JpaRepository;

@Transactional(readOnly = true)
public interface TagRepository  extends JpaRepository<Tag, Long>{
	Tag findByTitle(String title);
}
