-- Crear tabla Producto si no existe
IF NOT EXISTS (SELECT * FROM sys.objects WHERE object_id = OBJECT_ID(N'[dbo].[Producto]') AND type in (N'U'))
BEGIN
    CREATE TABLE [dbo].[Producto](
        [id] [bigint] IDENTITY(1,1) NOT NULL,
        [nombre] [nvarchar](255) NOT NULL,
        [descripcion] [nvarchar](max) NULL,
        [codigobarras] [nvarchar](255) NULL,
        [precio] [decimal](10, 2) NOT NULL,
        [stock] [int] NOT NULL,
        [categoria] [nvarchar](255) NULL,
        [imagen] [nvarchar](255) NULL,
        CONSTRAINT [PK_Producto] PRIMARY KEY CLUSTERED ([id] ASC),
        CONSTRAINT [UK_Producto_CodigoBarras] UNIQUE NONCLUSTERED ([codigobarras] ASC)
    )
END

-- Eliminar tabla Proveedor si existe
IF EXISTS (SELECT * FROM sys.objects WHERE object_id = OBJECT_ID(N'[dbo].[Proveedor]') AND type in (N'U'))
BEGIN
    DROP TABLE [dbo].[Proveedor]
END

-- Crear tabla Proveedor
CREATE TABLE [dbo].[Proveedor](
    [id] [bigint] IDENTITY(1,1) NOT NULL,
    [nombre] [nvarchar](255) NOT NULL,
    [contacto] [nvarchar](255) NOT NULL,
    [email] [nvarchar](255) NULL,
    [direccion] [nvarchar](500) NULL,
        CONSTRAINT [PK_Proveedor] PRIMARY KEY CLUSTERED ([id] ASC)
    )

-- Crear tabla LimiteStock si no existe
IF NOT EXISTS (SELECT * FROM sys.objects WHERE object_id = OBJECT_ID(N'[dbo].[LimiteStock]') AND type in (N'U'))
BEGIN
    CREATE TABLE [dbo].[LimiteStock](
        [id] [bigint] IDENTITY(1,1) NOT NULL,
        [limiteMinimo] [int] NOT NULL,
        [producto_id] [bigint] NOT NULL,
        CONSTRAINT [PK_LimiteStock] PRIMARY KEY CLUSTERED ([id] ASC),
        CONSTRAINT [FK_LimiteStock_Producto] FOREIGN KEY([producto_id]) REFERENCES [dbo].[Producto] ([id])
    )
END

-- Crear tabla Usuario si no existe
IF NOT EXISTS (SELECT * FROM sys.objects WHERE object_id = OBJECT_ID(N'[dbo].[Usuario]') AND type in (N'U'))
BEGIN
    CREATE TABLE [dbo].[Usuario](
        [id] [bigint] IDENTITY(1,1) NOT NULL,
        [username] [nvarchar](255) NOT NULL,
        [password] [nvarchar](255) NOT NULL,
        [email] [nvarchar](255) NOT NULL,
        CONSTRAINT [PK_Usuario] PRIMARY KEY CLUSTERED ([id] ASC),
        CONSTRAINT [UK_Usuario_Username] UNIQUE NONCLUSTERED ([username] ASC),
        CONSTRAINT [UK_Usuario_Email] UNIQUE NONCLUSTERED ([email] ASC)
    )
END

-- Crear tabla Rol si no existe
IF NOT EXISTS (SELECT * FROM sys.objects WHERE object_id = OBJECT_ID(N'[dbo].[Rol]') AND type in (N'U'))
BEGIN
    CREATE TABLE [dbo].[Rol](
        [id] [bigint] IDENTITY(1,1) NOT NULL,
        [nombre] [nvarchar](50) NOT NULL,
        CONSTRAINT [PK_Rol] PRIMARY KEY CLUSTERED ([id] ASC)
    )
END

-- Crear tabla usuario_roles si no existe
IF NOT EXISTS (SELECT * FROM sys.objects WHERE object_id = OBJECT_ID(N'[dbo].[usuario_roles]') AND type in (N'U'))
BEGIN
    CREATE TABLE [dbo].[usuario_roles](
        [usuario_id] [bigint] NOT NULL,
        [rol_id] [bigint] NOT NULL,
        CONSTRAINT [PK_usuario_roles] PRIMARY KEY CLUSTERED ([usuario_id] ASC, [rol_id] ASC),
        CONSTRAINT [FK_usuario_roles_usuario] FOREIGN KEY([usuario_id]) REFERENCES [dbo].[Usuario] ([id]),
        CONSTRAINT [FK_usuario_roles_rol] FOREIGN KEY([rol_id]) REFERENCES [dbo].[Rol] ([id])
    )
END
