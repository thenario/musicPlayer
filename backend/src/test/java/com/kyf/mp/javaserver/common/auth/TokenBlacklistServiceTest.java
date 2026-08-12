// package com.kyf.mp.javaserver.common.auth;

// import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
// import static org.junit.jupiter.api.Assertions.assertFalse;
// import static org.junit.jupiter.api.Assertions.assertTrue;

// import com.kyf.mp.javaserver.utils.JwtUtils;
// import java.lang.reflect.Field;
// import java.util.Map;
// import org.junit.jupiter.api.BeforeEach;
// import org.junit.jupiter.api.Test;

// class TokenBlacklistServiceTest {

//     private TokenBlacklistService service;

//     @BeforeEach
//     void setUp() {
//         // JwtUtils 的 SECRET / EXPIRE 是静态字段，由 @Value 注入，测试时手动赋值
//         setStatic(JwtUtils.class, "SECRET", "a".repeat(64));
//         setStatic(JwtUtils.class, "EXPIRE", 86_400_000L);
//         service = new TokenBlacklistService(86_400_000L);
//     }

//     @Test
//     void revokeThenReject() {
//         String token = JwtUtils.createToken(1, "kyf");
//         service.revoke(token);
//         assertTrue(service.isRevoked(token));
//     }

//     @Test
//     void unknownTokenIsNotRevoked() {
//         assertFalse(service.isRevoked("not-a-real-token"));
//     }

//     @Test
//     void revokeExpiredTokenIsNoop() {
//         String valid = JwtUtils.createToken(1, "kyf");
//         service.revoke(valid);

//         setStatic(JwtUtils.class, "EXPIRE", 0L); // 之后的 token 立即过期
//         String expired = JwtUtils.createToken(1, "kyf");

//         assertDoesNotThrow(() -> service.revoke(expired));
//         assertFalse(service.isRevoked(expired)); // 过期 token 解析失败，不入黑名单
//         assertTrue(service.isRevoked(valid));    // 之前吊销的有效 token 不受影响
//     }

//     @Test
//     @SuppressWarnings("unchecked")
//     void purgeExpiredCleansUp() {
//         String token = JwtUtils.createToken(1, "kyf");
//         service.revoke(token);
//         assertTrue(service.isRevoked(token));

//         // 把条目过期时刻改到过去，验证定期清理能回收
//         Field revokedField;
//         try {
//             revokedField = TokenBlacklistService.class.getDeclaredField("revoked");
//             revokedField.setAccessible(true);
//         } catch (NoSuchFieldException e) {
//             throw new IllegalStateException("internal map field not found", e);
//         }
//         Object mapValue;
//         try {
//             mapValue = revokedField.get(service);
//         } catch (IllegalAccessException e) {
//             throw new IllegalStateException("cannot read internal map", e);
//         }
//         Map.Entry<String, Long> entry = ((Map<String, Long>) mapValue).entrySet().iterator().next();
//         entry.setValue(System.currentTimeMillis() - 1000);

//         service.purgeExpired();
//         assertFalse(service.isRevoked(token));
//     }

//     private static void setStatic(Class<?> clazz, String fieldName, Object value) {
//         try {
//             Field field = clazz.getDeclaredField(fieldName);
//             field.setAccessible(true);
//             field.set(null, value);
//         } catch (ReflectiveOperationException e) {
//             throw new IllegalStateException("Failed to set static field " + fieldName, e);
//         }
//     }
// }
