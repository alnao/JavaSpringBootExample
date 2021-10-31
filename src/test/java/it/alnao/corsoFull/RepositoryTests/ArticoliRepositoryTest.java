package it.alnao.corsoFull.RepositoryTests;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.Assert.assertEquals;

import java.util.List;

import com.xantrix.webapp.Application;
import com.xantrix.webapp.entity.Articoli;
import com.xantrix.webapp.repository.ArticoliRepository;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.domain.PageRequest;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;


@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(classes = Application.class)
@SpringBootTest
//@FixMethodOrder(MethodSorters.NAME_ASCENDING)
public class ArticoliRepositoryTest
{
	@Autowired
	private ArticoliRepository articoliRepository;

	@Test
	public void TestSelByDescrizioneLike()
	{
		List<Articoli> items = articoliRepository.SelByDescrizioneLike("ACQUA ULIVETO%");
		assertEquals(2, items.size());
	}
	
	@Test
	public void TestfindByDescrizioneLikePage()
	{
		List<Articoli> items = articoliRepository.findByDescrizioneLike("ACQUA%",PageRequest.of(0, 10));
		assertEquals(10, items.size());
	}

	
	@Test
	public void TestfindByCodArt() throws Exception
	{
		assertThat(articoliRepository.findByCodArt("002000301"))
				.extracting(Articoli::getDescrizione)
				.isEqualTo("ACQUA ULIVETO 15 LT");
				
	}
	

}
