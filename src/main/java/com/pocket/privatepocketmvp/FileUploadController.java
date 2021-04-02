package com.pocket.privatepocketmvp;

import com.pocket.privatepocketmvp.storage.FileDB;
import com.pocket.privatepocketmvp.storage.FileStorageService;
import com.pocket.privatepocketmvp.storage.ResponseFile;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.method.annotation.MvcUriComponentsBuilder;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import java.io.IOException;
import java.util.List;
import java.util.stream.Collectors;

@Controller
public class FileUploadController {

    @Autowired
    private FileStorageService storageService;

    @Autowired
    public FileUploadController(FileStorageService storageService) {
        this.storageService = storageService;
    }

    @GetMapping("/")
    public String listUploadedFiles(Model model) throws IOException {

        List<ResponseFile> files = storageService.getAllFiles().map(dbFile -> {
            String fileDownloadUri = ServletUriComponentsBuilder
                    .fromCurrentContextPath()
                    .path("/files/")
                    .path(dbFile.getId())
                    .toUriString();

            return new ResponseFile(
                    dbFile.getName(),
                    fileDownloadUri,
                    dbFile.getType(),
                    dbFile.getData().length);
        }).collect(Collectors.toList());

        //model.addAttribute("files", files);

        model.addAttribute("files", storageService.getAllFiles().map(
                dbFile -> MvcUriComponentsBuilder.fromMethodName(FileUploadController.class,
                        "getFile", dbFile.getId()).build().toUri().toString())
                .collect(Collectors.toList()));

        return "uploadForm";
    }

    @PostMapping("/")
    public String handleFileUpload(@RequestParam("file") MultipartFile file,
                                   RedirectAttributes redirectAttributes) {
        try {
            storageService.store(file);
            redirectAttributes.addFlashAttribute("message", "You successfully uploaded " + file.getOriginalFilename() + "!");
            return "redirect:/";
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("message", "Could not upload the file: " + file.getOriginalFilename() + "!");
            return "redirect:/";
        }
    }

    @GetMapping("/files/{id}")
    public ResponseEntity<byte[]> getFile(@PathVariable String id) {
        FileDB fileDB = storageService.getFile(id);

        byte[] image = storageService.getFile(id).getData();
        return ResponseEntity.ok().contentType(MediaType.IMAGE_JPEG).body(image);

        /*return ResponseEntity.ok().header(HttpHeaders.CONTENT_DISPOSITION,
                "attachment; filename=\"" + fileDB.getName() + "\"").body(fileDB.getData());*/
    }
}
