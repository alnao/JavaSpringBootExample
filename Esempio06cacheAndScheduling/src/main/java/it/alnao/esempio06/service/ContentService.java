package it.alnao.esempio06.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

import it.alnao.esempio06.entity.Content;
import it.alnao.esempio06.repository.ContentRepository;

import java.util.List;

@Service
public class ContentService {

    @Autowired
    private ContentRepository contentRepository;

    @Cacheable("contents")
    public List<Content> getAllContents() {
        return contentRepository.findAll();
        }
    @CacheEvict(value = "contents", key = "#content.id")
    public void updateContent(Content content) {
        contentRepository.save(content);
    }

    @CacheEvict(value = "contents", key = "#content.id")
    public void deleteContent(Content content) {
        contentRepository.delete(content);
    }

    @CacheEvict(value = "contents", allEntries = true)
    public Content addOrUpdateContent(Content content) {
        return contentRepository.save(content);
    }

    @Cacheable("lastContent") 
    public Content getLastInsertedContent() {
        return contentRepository.findTopByOrderByIdDesc();
    }
}
