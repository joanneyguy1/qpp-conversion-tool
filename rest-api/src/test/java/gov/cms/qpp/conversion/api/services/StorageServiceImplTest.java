package gov.cms.qpp.conversion.api.services;


import com.amazonaws.services.s3.model.PutObjectRequest;
import com.amazonaws.services.s3.model.PutObjectResult;
import com.amazonaws.services.s3.model.S3Object;
import com.amazonaws.services.s3.transfer.TransferManager;
import com.amazonaws.services.s3.transfer.Upload;
import com.amazonaws.services.s3.transfer.internal.UploadImpl;
import gov.cms.qpp.conversion.api.RestApiApplication;
import net.jodah.concurrentunit.Waiter;
import org.apache.commons.io.IOUtils;
import org.junit.Before;
import org.junit.ClassRule;
import org.junit.Rule;
import org.junit.Test;
import org.mockito.Mock;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.PropertySource;
import org.springframework.test.context.junit4.rules.SpringClassRule;
import org.springframework.test.context.junit4.rules.SpringMethodRule;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeoutException;

import static org.junit.Assert.fail;
import static org.mockito.ArgumentMatchers.any;
import static org.powermock.api.mockito.PowerMockito.when;


@SpringBootTest(classes = { StorageServiceImpl.class, RestApiApplication.class })
@PropertySource("classpath:application.properties")
public class StorageServiceImplTest {
	@ClassRule
	public static final SpringClassRule SPRING_CLASS_RULE = new SpringClassRule();

	@Rule
	public final SpringMethodRule springMethodRule = new SpringMethodRule();

	@Autowired
	private StorageServiceImpl underTest;

	@Mock
	private TransferManager transferManager;

	@Mock
	private Upload upload;

	@Test
	public void testPut() throws TimeoutException {
		Upload upload = new UploadImpl();
		when(transferManager.upload(any(PutObjectRequest.class))).thenReturn();
		final String content = "test file content";
		final String key = "submission";
		final Waiter waiter = new Waiter();

		CompletableFuture<String> result = underTest.store(
				key, new ByteArrayInputStream(content.getBytes()));

		result.whenComplete((outcome, ex) -> {
			System.out.println("outcome: " + outcome);
			waiter.assertEquals(content, getObjectContent(key));
			waiter.resume();
		});

		waiter.await(5000);
	}

	private String getObjectContent(String key) {
		S3Object stored = amazonS3Client.getObject(bucketName, key);

		try {
			return IOUtils.toString(stored.getObjectContent(), "UTF-8");
		} catch (IOException ioe) {
			fail("should have content");
		}
		return "";
	}
}
