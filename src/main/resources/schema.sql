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
