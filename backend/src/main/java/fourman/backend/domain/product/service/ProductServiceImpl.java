package fourman.backend.domain.product.service;

import fourman.backend.domain.product.controller.dto.ImageResourceResponse;
import fourman.backend.domain.product.controller.dto.ProductListResponse;
import fourman.backend.domain.product.controller.dto.ProductRequest;
import fourman.backend.domain.product.entity.ImageResource;
import fourman.backend.domain.product.entity.Product;
import fourman.backend.domain.product.repository.ImageResourceRepository;
import fourman.backend.domain.product.repository.ProductRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import javax.transaction.Transactional;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class ProductServiceImpl implements ProductService {

    final private ProductRepository productRepository;
    final private ImageResourceRepository imageResourceRepository;

    @Transactional
    @Override
    public void register(List<MultipartFile> imageFileList, ProductRequest productRequest) {

        List<ImageResource> imageResourceList = new ArrayList<>();

        final String fixedStringPath = "../../FourMan-Front/frontend/src/assets/product/uploadImgs/";

        Product product = new Product();

        product.setProductName(productRequest.getProductName());
        product.setPrice(productRequest.getPrice());

        try{
            for(MultipartFile multipartFile: imageFileList) {
                log.info("requestFileUploadWithText() - filename: " + multipartFile.getOriginalFilename());

                String fullPath = fixedStringPath + multipartFile.getOriginalFilename();

                FileOutputStream writer = new FileOutputStream(fullPath);

                writer.write(multipartFile.getBytes());
                writer.close();

                ImageResource imageResource = new ImageResource(multipartFile.getOriginalFilename());
                imageResourceList.add(imageResource);
                product.setImageResource(imageResource);
            }
        } catch(FileNotFoundException e) {
            e.printStackTrace();
        } catch(IOException e) {
            e.printStackTrace();
        }

        productRepository.save(product);

        imageResourceRepository.saveAll(imageResourceList);

    }

    @Override
    public List<ProductListResponse> list() {
        List<Product> productList = productRepository.findAll();
        List<ProductListResponse> productResponseList = new ArrayList<>();

        for(Product product: productList) {
            productResponseList.add(new ProductListResponse(
                    product.getProductId(), product.getProductName(),
                    product.getPrice()
            ));
        }

        return productResponseList;
    }

    @Override
    public List<ImageResourceResponse> loadProductImage() {
        List<ImageResource> imageResourceList = imageResourceRepository.findAll();
        List<ImageResourceResponse> imageResourceResponseList = new ArrayList<>();

        for(ImageResource imageResource: imageResourceList) {
            System.out.println("imageResource Path: " + imageResource.getImageResourcePath());

            imageResourceResponseList.add(new ImageResourceResponse(
                    imageResource.getImageResourcePath()
            ));
        }

        return imageResourceResponseList;
    }
}
