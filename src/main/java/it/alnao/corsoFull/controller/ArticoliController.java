package it.alnao.corsoFull.controller;

import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.xantrix.webapp.entity.Articoli;
import com.xantrix.webapp.entity.Barcode;
import com.xantrix.webapp.exception.NotFoundException;
import com.xantrix.webapp.service.ArticoliService;
import com.xantrix.webapp.service.BarcodeService;

@RestController
@RequestMapping("api/articoli")
@CrossOrigin(origins="http://localhost:4200")
public class ArticoliController 
{
	private static final Logger logger = LoggerFactory.getLogger(ArticoliController.class);
	
	@Autowired
	private ArticoliService articoliService;
	
	@Autowired
	private BarcodeService barcodeService;
	
	@GetMapping(value = "/cerca/ean/{barcode}", produces = "application/json")
	public ResponseEntity<Articoli> listArtByEan(@PathVariable("barcode") String Barcode)
			throws NotFoundException	
	{
		logger.info("****** Otteniamo l'articolo con barcode " + Barcode + " *******");
		
		Articoli articolo;
		Barcode Ean = barcodeService.SelByBarcode(Barcode);
		
		if (Ean == null)
		{
			String ErrMsg = String.format("Il barcode %s non è stato trovato!", Barcode);
			
			logger.warn(ErrMsg);
			
			throw new NotFoundException(ErrMsg);
			//return new ResponseEntity<Articoli>(HttpStatus.NOT_FOUND);
		}
		else
		{
			articolo = Ean.getArticolo();
		}
		
		return new ResponseEntity<Articoli>(articolo, HttpStatus.OK);
		
	}
	
	@GetMapping(value = "/cerca/codice/{codart}", produces = "application/json")
	public ResponseEntity<Articoli> listArtByCodArt(@PathVariable("codart") String CodArt)
			throws NotFoundException
	{
		logger.info("****** Otteniamo l'articolo con codice " + CodArt + " *******");
		
		Articoli articolo = articoliService.SelByCodArt(CodArt);
		
		if (articolo == null)
		{
			String ErrMsg = String.format("L'articolo con codice %s non è stato trovato!", CodArt);
			
			logger.warn(ErrMsg);
			
			throw new NotFoundException(ErrMsg);
		}
		
		return new ResponseEntity<Articoli>(articolo, HttpStatus.OK);
	}
	
	@GetMapping(value = "/cerca/descrizione/{filter}", produces = "application/json")
	public ResponseEntity<List<Articoli>> listArtByDesc(@PathVariable("filter") String Filter)
			throws NotFoundException
	{
		logger.info("****** Otteniamo gli articoli con Descrizione: " + Filter + " *******");
		
		List<Articoli> articoli = articoliService.SelByDescrizione(Filter.toUpperCase() + "%");
		
		if (articoli == null)
		{
			String ErrMsg = String.format("Non è stato trovato alcun articolo avente descrizione %s", Filter);
			
			logger.warn(ErrMsg);
			
			throw new NotFoundException(ErrMsg);
			
		}
		
		return new ResponseEntity<List<Articoli>>(articoli, HttpStatus.OK);
	}
}
