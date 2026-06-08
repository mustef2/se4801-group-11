INSERT INTO users (
    full_name,
    email,
    password,
    role,
    is_active,
    is_first_login
) VALUES (
           'System Administrator',
           'eyob.m.dev@gmail.com',
           '$2b$12$Pxv4pa2Hu0R2jnJCS4atMOf7My2ZCxCOshtosZHsR0/8zTnEDeCES',
           'ADMIN',
           true,
           false
       );