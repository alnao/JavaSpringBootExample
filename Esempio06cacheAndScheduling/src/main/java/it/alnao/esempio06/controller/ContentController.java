package it.alnao.esempio06.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;

import it.alnao.esempio06.entity.Content;
import it.alnao.esempio06.service.ContentService;

import java.util.List;

@RestController
@RequestMapping("/api")
public class ContentController {

    @Autowired
    private ContentService contentService;

    @GetMapping("/contents")
    public List<Content> getAllContents() {
        return contentService.getAllContents();
    }

    @PostMapping("/contents")
    public Content createContent(@RequestBody Content content) {
        return contentService.addOrUpdateContent(content);
    }

    @PutMapping("/contents/{id}")
    public void updateContent(@PathVariable Long id, @RequestBody Content updatedContent) {
        updatedContent.setId(id);
        contentService.updateContent(updatedContent);
    }

    @DeleteMapping("/contents/{id}")
    public void deleteContent(@PathVariable Long id) {
        Content content = new Content();
        content.setId(id);
        contentService.deleteContent(content);
    }

    @GetMapping("/contents/last")
    public Content getLastInsertedContent() {
        return contentService.getLastInsertedContent();
    }
}
