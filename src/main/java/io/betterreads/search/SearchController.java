package io.betterreads.search;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.reactive.function.client.ExchangeStrategies;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

import java.util.List;
import java.util.stream.Collectors;

@Controller
public class SearchController {

    private final String COVER_IMAGE_ROOT = "https://covers.openlibrary.org/b/id/";

    private final WebClient webClient;

    // Building the webclient only once (Not for every request the controller receives)
    public SearchController(WebClient.Builder webClientBuilder) {
        this.webClient = webClientBuilder
                .exchangeStrategies(
                        ExchangeStrategies.
                                builder().
                                codecs(configurer -> configurer.defaultCodecs().
                                        maxInMemorySize(16 * 1024 * 1024)).
                                build()
                )
                .baseUrl("https://openlibrary.org/search.json")
                .build();
    }

    @GetMapping(value = "/search")
    public String getSearchResults(@RequestParam String query, Model model) {
        Mono<SearchResult> response = this.webClient.get()
                .uri("?q={query}", query)
                .retrieve().bodyToMono(SearchResult.class);
        SearchResult result = response.block();
        List<SearchResultBook> books = result.getDocs()
                .stream()
                .limit(100)
                .map(bookResult -> {
                    bookResult.setKey(bookResult.getKey().replace("works/", ""));

                    String coverId = bookResult.getCover_i();
                    String coverUrl = "/images/no-image.png";
                    if (StringUtils.hasText(coverId)) {
                        coverUrl = COVER_IMAGE_ROOT +coverId + "-M.jpg";
                    }
                    bookResult.setCover_i(coverUrl);

                    return bookResult;
                })
                .collect(Collectors.toList());

        model.addAttribute("searchResult", books);

        return "search";
    }
}
