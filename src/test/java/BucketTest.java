import java.io.ByteArrayInputStream;
import java.io.InputStream;

import org.testng.AssertJUnit;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import com.amazonaws.AmazonServiceException;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.model.Bucket;
import com.amazonaws.services.s3.model.CreateBucketRequest;
import com.amazonaws.services.s3.model.ListObjectsRequest;
import com.amazonaws.services.s3.model.ObjectListing;
import com.amazonaws.services.s3.model.ObjectMetadata;
import com.amazonaws.services.s3.model.PutObjectRequest;
import com.amazonaws.util.StringUtils;

public class BucketTest {
	
	private static S3 utils =  new S3();
	AmazonS3 svc = utils.getCLI();
	String prefix = utils.getPrefix();

	@AfterMethod
	public  void tearDownAfterClass() throws Exception {
		
		utils.tearDown(svc);	
	}

	@BeforeMethod
	public void setUp() throws Exception {
	}

	@Test
	public void testBucketListEmpty() {
		
		String bucket_name = utils.getBucketName(prefix);
		svc.createBucket(new CreateBucketRequest(bucket_name));
		
		ObjectListing list = svc.listObjects(new ListObjectsRequest()
								.withBucketName(bucket_name));
		AssertJUnit.assertEquals(list.getObjectSummaries().isEmpty(), true);
	}
	
	@Test 
	public void testBucketDeleteNotExist() {
		
		String bucket_name = utils.getBucketName(prefix);
		AssertJUnit.assertEquals(svc.doesBucketExist(bucket_name), false);
		
		try {
			
			svc.deleteBucket(bucket_name);
		} catch (AmazonServiceException err) {
			AssertJUnit.assertEquals(err.getErrorCode(), "NoSuchBucket");
		}
		
	}
	
	@Test 
	public void testBucketDeleteNonEmpty() {
		
		String bucket_name = utils.getBucketName(prefix);
		svc.createBucket(new CreateBucketRequest(bucket_name));
		
		svc.putObject(bucket_name, "key1", "echo");
		
		try {
			
			svc.deleteBucket(bucket_name);
		} catch (AmazonServiceException err) {
			AssertJUnit.assertEquals(err.getErrorCode(), "BucketNotEmpty");
		}
		
	}
	
	@Test 
	public void testBucketCreateReadDelete() {
		
		String bucket_name = utils.getBucketName(prefix);
		svc.createBucket(new CreateBucketRequest(bucket_name));
		AssertJUnit.assertEquals(svc.doesBucketExist(bucket_name), true);
		
		svc.deleteBucket(bucket_name);
		AssertJUnit.assertEquals(svc.doesBucketExist(bucket_name), false);
		
	}
	
	@Test
	public void testBucketListDistinct() {
		
		String bucket1 = utils.getBucketName(prefix);
		String bucket2 = utils.getBucketName(prefix);
		
		svc.createBucket(new CreateBucketRequest(bucket1));
		svc.createBucket(new CreateBucketRequest(bucket2));
		
		svc.putObject(bucket1, "key1", "echo");
		
		ObjectListing list = svc.listObjects(new ListObjectsRequest()
				.withBucketName(bucket2));
		AssertJUnit.assertEquals(list.getObjectSummaries().isEmpty(), true);
	}
	
	@Test
	public void testBucketNotExist() {
		
		String bucket_name = utils.getBucketName(prefix);
		try {
			
			svc.getBucketAcl(bucket_name);
		} catch (AmazonServiceException err) {
			AssertJUnit.assertEquals(err.getErrorCode(), "NoSuchBucket");
		}
	}
	
	@Test
	//@Description("create w/expect 200, garbage but S3 succeeds!")
	public void testBucketCreateBadExpectMismatch() {
		
		String bucket_name = utils.getBucketName(prefix);
		
		CreateBucketRequest bktRequest = new CreateBucketRequest(bucket_name);
		bktRequest.putCustomRequestHeader("Expect", "200");
		 svc.createBucket(bktRequest);
	}
	
	@Test
	//@Description("create w/expect empty, garbage but S3 succeeds!")
	public void testBucketCreateBadExpectEmpty() {
		
		String bucket_name = utils.getBucketName(prefix);
		
		CreateBucketRequest bktRequest = new CreateBucketRequest(bucket_name);
		bktRequest.putCustomRequestHeader("Expect", "");
		svc.createBucket(bktRequest);
	}
	
	@Test
	//@Description("create w/expect empty, garbage but S3 succeeds!")
	public void testBucketCreateBadExpectUnreadable() {
		
		String bucket_name = utils.getBucketName(prefix);
		
		CreateBucketRequest bktRequest = new CreateBucketRequest(bucket_name);
		bktRequest.putCustomRequestHeader("Expect", "\\x07");
		svc.createBucket(bktRequest);
	}
	

	@Test
	public void TestObjectCreateBadMd5Empty() {
		
		try {
			
			String bucket_name = utils.getBucketName(prefix);
			String key = "key1";
			String content = "echo lima golf";
			String md5 = " ";
				
			svc.createBucket(new CreateBucketRequest(bucket_name));

			byte[] contentBytes = content.getBytes(StringUtils.UTF8);
			InputStream is = new ByteArrayInputStream(contentBytes);
				
			ObjectMetadata metadata = new ObjectMetadata();
			metadata.setContentLength(contentBytes.length);
			metadata.setHeader("Content-MD5", md5);
				
			svc.putObject(new PutObjectRequest(bucket_name, key, is, metadata));
			
		} catch (AmazonServiceException err) {
			AssertJUnit.assertEquals(err.getErrorCode(), "400 Bad Requ]est");
		}
		
	}

	@Test
	//@Description("create w/non-graphic content length, succeeds")
	public void testBucketCreateContentlengthUnreadable() {
		
		String bucket_name = utils.getBucketName(prefix);
		
		try {
			
		CreateBucketRequest bktRequest = new CreateBucketRequest(bucket_name);
		bktRequest.putCustomRequestHeader("Content-Length", "\\x07");
		svc.createBucket(bktRequest);
		} catch (AmazonServiceException err) {
			AssertJUnit.assertEquals(err.getErrorCode(), "SignatureDoesNotMatch");
		}
	}

	@Test
	public void testBucketCreateContentlengthNone() {
		try {
			
			String bucket_name = utils.getBucketName(prefix);
			
			CreateBucketRequest bktRequest = new CreateBucketRequest(bucket_name);
			bktRequest.putCustomRequestHeader("Content-Length", "");
			svc.createBucket(bktRequest);
			
		} catch (AmazonServiceException err) {
			AssertJUnit.assertEquals(err.getErrorCode(), "SignatureDoesNotMatch");
		}
	}

	@Test
	public void testBucketCreateContentlengthEmpty() {
		
		String bucket_name = utils.getBucketName(prefix);
		
		try {
			
		
			CreateBucketRequest bktRequest = new CreateBucketRequest(bucket_name);
			bktRequest.putCustomRequestHeader("Content-Length", " ");
			svc.createBucket(bktRequest);
		} catch (AmazonServiceException err) {
			AssertJUnit.assertEquals(err.getErrorCode(), "SignatureDoesNotMatch");
		}
	}
	
	@Test
	public void testBucketCreateBadAuthorizationUnreadable() {
		
		String bucket_name = utils.getBucketName(prefix);
		
		try {
			
		CreateBucketRequest bktRequest = new CreateBucketRequest(bucket_name);
		bktRequest.putCustomRequestHeader("Authorization", "\\x07");
		svc.createBucket(bktRequest);
		} catch (AmazonServiceException err) {
			AssertJUnit.assertEquals(err.getErrorCode(), "SignatureDoesNotMatch");
		}
	}

	@Test
	public void testBucketCreateBadAuthorizationEmpty() {
		
		String bucket_name = utils.getBucketName(prefix);
		
		try {
			
		CreateBucketRequest bktRequest = new CreateBucketRequest(bucket_name);
		bktRequest.putCustomRequestHeader("Authorization", "");
		svc.createBucket(bktRequest);
		} catch (AmazonServiceException err) {
			AssertJUnit.assertEquals(err.getErrorCode(), "SignatureDoesNotMatch");
		}
	}
	
	@Test
	public void testBucketCreateBadAuthorizationNone() {
		
		String bucket_name = utils.getBucketName(prefix);
		
		try {
			
		CreateBucketRequest bktRequest = new CreateBucketRequest(bucket_name);
		bktRequest.putCustomRequestHeader("Authorization", " ");
		svc.createBucket(bktRequest);
		} catch (AmazonServiceException err) {
			AssertJUnit.assertEquals(err.getErrorCode(), "SignatureDoesNotMatch");
		}
	}
	

	
}
